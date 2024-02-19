#version 330

const int MAX_POINT_LIGHTS  = 100;
const int MAX_SPOT_LIGHTS   = 5;
const int NUM_CASCADES      = 3;
const vec2 SHADOW_INC_MAP_0 = 1.0 / vec2(2048, 2048) * 0.5;
const int SHADOW_RANGE_PCF  = 1;

in vec2  vs_textCoord;
in vec3  vs_mvNormal;
in vec3  vs_mvVertexPos;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat4  vs_modelViewMatrix;
in float vs_selected;

//Lumiere
in vec3  vs_cameraDirection;

out vec4 fs_color;

// L'effet d'attenuation de la lumiere en fonction de la distance
struct Attenuation 
{
	float constant;
	float linear;
	float exponent;
};

// La source de lumiere, sa couleur, sa position, son intensite.
// Et son atténuation
struct PointLight
{
	vec3 colour;
	vec3 position;
	float intensity;
	Attenuation att;
};

// La source de lumière, sa direction et son angle ( spot light est comme une lampe de chevet)
struct SpotLight
{
	PointLight pl;
	vec3 conedir;
	float cutoff;
};

// La couleur, la direction et son intensite, directional light est symbolise par exemple par le soleil
struct DirectionalLight
{
	vec3 colour;
	vec3 direction;
	float intensity;
};

// Les données du materiaux, sa couleur ambiente, sa couleur diffusé et sa lumiere spéculaire.
// Si il a une texture (hasTexture) et une normal map (hasNormalMap) et enfin sa capacite a réflechir (reflectance)
struct Material
{
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	bool hasTexture;
	bool hasNormalMap;
	float reflectance;
};

// Les donnes relative au brouillard
struct Fog
{
	// Si il y a du brouillard ( 0 ou 1 )
	int activeFog;
	vec3 colour; // sa couleur
	float density; // son facteur de densité 
};

// Uniform désigne les donnes qui vont être envoye par le code Java (par setUniform)
uniform sampler2D texture_sampler;
uniform sampler2D normalMap;
uniform sampler2D zmap;

uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;

uniform Fog fog;

// Nous nutiliserons pas qu'un spot light et qu'un point lights. Nous allons creer donc une liste.
// En glsl, les listes doiven être définie avec une longueur de base, cette longueur est définie pâr les deux constantes placé plus haut (5).
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];

// Nous definisons qu'une seule lumière directionelle (le soleil)
uniform DirectionalLight directionalLight;

// Sha
uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform bool renderShadow;

// Variable globale
vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

// Définie les couleurs utilisé en fonction du materiel
void setupColours(in Material material, in vec2 textCoord)
{
	// Si le materiel a une texture d'appliqué
	if ( material.hasTexture ) 
	{
		// Ses couleurs diffuse et spéculer vont être égale a sa texture 
		ambientC = texture(texture_sampler, textCoord); // Attribue la bonne texture en fonction des coordonnés de texture
		diffuseC = ambientC;
		speculrC = ambientC;
	}
	else // Sinon ses couleurs sont égale a celle définie par le materiel
	{
		ambientC = material.ambient;
		diffuseC = material.diffuse;
		speculrC = material.specular;
	}

	if ( ambientC.w == 0 ) {
			discard;
	}
}

// Calcul la lumière émise et qui est renvoyé a notre oeil, l'attenuation par la distance n'est pas pris en compte
vec4 calcLightColour(in vec3 light_colour, in float light_intensity, in vec3 to_light_dir, in vec3 normal)
{
	// La lumiere diffusé

	// Moins un objet est orienté vers la source de lumiere moins il est éclairé, le facteur de diffusion
	// est donc calculer par l'ecart entre le direction de la source de lumiere et la tangente du point.
	float diffuseFactor = max( dot(normal, to_light_dir), 0.0); // Le résultat doit etre supérieur ou égale a 0

	// Enfin, la lumière diffusé est donc la lumiere diffusé normalement par l'objet (sa couleur), multiplié par la couleur 
	// de la source de lumiere multiplier par l'intensité de la lumiere multiplier par le facteur de diffusion
	vec4 light = vec4(light_colour, 1.0) * light_intensity;
	vec4 diffuseColour = diffuseC * light * diffuseFactor;

	// La lumière spéculer ( la lumiere renvoyer a notre oeil, donc a la caméra)
	// La direction de la camera est donc l'inverse de lposition car la camera est placé a l'origine
	vec3 reflected_light = normalize( reflect( -to_light_dir, normal));
	
	float specularFactor = max( dot(vs_cameraDirection, reflected_light), 0.0); // Le résultat doit etre supérieur a 0
	specularFactor = pow(specularFactor, specularPower);

	vec4 specColour = speculrC * specularFactor * material.reflectance * light;

	// La couleur renvoyé est donc la lumiere diffuse plus la lumière retourné a notre oeil
	return (diffuseColour + specColour);
}

// Calcule comment le point est illuminé en fonction de sa position dans l'espace
vec4 calcPointLight(in vec3 light_direction, in vec3 normal, in PointLight light)
{
    vec3 to_light_dir  = normalize(light_direction); // On normalise ce vecteur
    vec4 light_colour = calcLightColour(light.colour, light.intensity, to_light_dir, normal);

    // L'attenuation de la lumière en fonction de la distance
	float distance = length(light_direction); // La distance entre la source de lumière est le point a illuminer
	// Le facteur d'attenuation de couleur est définie par la formule mathématique suivante : 
	float attenuationInv = light.att.constant + light.att.linear * distance + light.att.exponent * pow(distance, 2);

    // La couleur renvoyé est donc la lumiere diffuse plus la lumière retourné a notre oeil, le tout diviser par le facteur d'attenuation
    return light_colour / attenuationInv;
}

// Calcule comment le point est illuminé en fonction de sa position dans l'espace
vec4 calcPointLight(in PointLight light, in vec3 position, in vec3 normal)
{
    return calcPointLight(light.position - position, normal, light);
}

vec4 calcSpotLight(in SpotLight light, in vec3 position, in vec3 normal)
{
	vec3 light_direction = light.pl.position - position;
	// Même raisonnement, on determine le vecteur qui relie la source de lumiere et l'objet puis on le normalise
	vec3 from_light_dir = -normalize(light_direction); // On veut le vecteur qui part de la source, donc c l'opposé

	// Détermine l'orientation du rayon de lumiere par rapport a la direction du SpotLight
	float spot_alfa = dot( from_light_dir, normalize(light.conedir) );

    vec4 colour = vec4(0, 0, 0, 0);
	// Si la lumière est dans l'angle de lumière du SpotLight, alors on peut applique la lumière
	if ( spot_alfa > light.cutoff )
	{
		// SpotLight est comme un PointLight alors c la même formule
		colour = calcPointLight(light_direction, normal, light.pl);

		// La seule chose qui change, plus l'objet est situé a l'extremité du cone moins il sera illuminé. La lumière est plus intense au milieu du cone.
		// Exactement comme une lampe de chevet. Cette attenuation est définie par la formule mathématique suivante.
		// attenuation = 1 - ( 1 - cos(a)) / (1 - cos(cutOffAngle))
		colour *= (1.0 - (1.0 - spot_alfa) / (1.0 - light.cutoff));
	}

	// Sinon, si c'est à l'exterieur du cone de lumière alors il n'y as pas de calcul de lumière
	return colour;
}

vec4 calcDirectionnalLight(in DirectionalLight light, in vec3 normal)
{
	return calcLightColour(light.colour, light.intensity, normalize(light.direction), normal);
}

vec4 calcFog(in vec3 pos, in vec4 colour, in Fog fog, in vec3 ambientLight, in DirectionalLight directionalLight)
{
	float distance = length(pos);

    // Fromule mathématique pour la facteur : 1 / e(distance * density)^2
    // Plus on est loin plus on a l'impression qu'il y a de brouillard
    float fogFactor = 1.0 / exp( pow(distance * fog.density, 2));

    fogFactor = clamp( fogFactor, 0.0, 1.0 ); // On veut que le facteur soit entre 0 et 1, ce que fait la fonction clamp

	// On calcul la lumiere du brouillard en tenant compte de la lumiere ambiante et de la lumiere du "soleil".
	vec3 fogColour = fog.colour * (ambientLight + directionalLight.colour * directionalLight.intensity);

	// La couleur finale est donc égale au produit de la couleur du brouillard, de la couleur du fragment le tout multiplie par le facteur de brouillard
	vec3 resultColour = mix( fogColour, colour.xyz, fogFactor );

	return vec4(resultColour.xyz, colour.w); // On préserve alpha ( w )
}

vec3 calcNormal(in Material material, in vec3 normal, in vec2 text_coord, in mat4 outModelViewMatrix)
{
	if ( material.hasNormalMap )
	{
		vec3 newNormal = texture(normalMap, text_coord).rgb;
		newNormal = normalize(newNormal * 2 - 1);

		return normalize(outModelViewMatrix * vec4(newNormal, 0.0)).xyz;
	}
	return normal;
}

float calcShadow(in vec4 position, in int idx)
{
	if ( !renderShadow ) {
		return 1.0;
	}

	vec3 projCoord = position.xyz * 0.5 + 0.5;

	float shadowFactor = 0.0, bias = 0.005;
	float depth = projCoord.z - bias;

	for(int row = -SHADOW_RANGE_PCF; row <= SHADOW_RANGE_PCF; ++row)
	{
		for(int col = -SHADOW_RANGE_PCF; col <= SHADOW_RANGE_PCF; ++col)
		{
			float textDepth;
			if ( idx == 0) 
			{
				textDepth = texture(shadowMap_0, projCoord.xy + vec2(row, col) * SHADOW_INC_MAP_0).r;
			}
			else if ( idx == 1)
			{
				textDepth = texture(shadowMap_1, projCoord.xy + vec2(row, col) * SHADOW_INC_MAP_0).r;	
			}
			else
			{
				textDepth = texture(shadowMap_2, projCoord.xy + vec2(row, col) * SHADOW_INC_MAP_0).r;
			}
			shadowFactor += depth > textDepth ? 0.7 : 0.0;
		}
	}

	shadowFactor /= 9;

	if( position.z > 1.0)
	{
		shadowFactor = 1.0;
	}

	return 1 - shadowFactor;
}

void main()
{
	float depth = texture(zmap, gl_FragCoord.xy).r;
	if ( vs_mvVertexPos.z >= depth) {
		discard;
	}


	// Initialise les couleur du materiel
	setupColours(material, vs_textCoord);

	vec3 currNormal = calcNormal(material, vs_mvNormal, vs_textCoord, vs_modelViewMatrix);

	// On récupere la lumière en fonction de la position du point
	vec4 diffuseSpecularComp = calcDirectionnalLight(directionalLight, currNormal);


	for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
		// Comment savoir lorsqu'un element de la liste est present ou non ? On verifie son intensite, si elle est a 0, on saute l'element
		if ( pointLights[i].intensity > 0) 
		{
			diffuseSpecularComp += calcPointLight(pointLights[i], vs_mvVertexPos, currNormal);
		}
		else
		{
			break;
		}
	}

	for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
		// Même raisonnement qu'au dessus
		if( spotLights[i].pl.intensity > 0)
		{
			diffuseSpecularComp += calcSpotLight(spotLights[i], vs_mvVertexPos, currNormal);
		}
	}

	float shadow = 1;
	for ( int i = 0; i < NUM_CASCADES; i++ )
	{
		if ( abs( vs_mvVertexPos.z ) < cascadeFarPlanes[i] )
		{
			shadow = calcShadow(vs_mlightviewVertexPos[i], i);
			break;
		}
	}

	// La couleur finale est donc la couleur du materiel multiplier par la couleur ambient plus le lumière du point dans l'espace
	fs_color = clamp(ambientC * vec4(ambientLight, 1) + diffuseSpecularComp * shadow, 0, 1);

	if ( fog.activeFog == 1 ) {
		fs_color = calcFog(vs_mvVertexPos, fs_color, fog, ambientLight, directionalLight);
	}

	if ( vs_selected > 0 ) {
		fs_color = vec4(fs_color.x, fs_color.y, 1, fs_color.w);
	}
}

