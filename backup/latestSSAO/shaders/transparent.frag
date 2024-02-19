#version 330

#define NUM_CASCADES 3
#define SHADOW_BIAS 0.005

const int MAX_POINT_LIGHTS  = 100;
const int MAX_SPOT_LIGHTS   = 5;
const vec4 BLACK            = vec4(0, 0, 0, 1);

in vec2  vs_textCoord;
in vec3  vs_mvNormal;
in float vs_mvDepth;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat3  vs_normalMatrix;

out vec4 fs_color;

struct Material
{
	vec4  diffuse;
	float specularFactor;
	vec3  emissive;

	bool hasDiffuseMap;
	bool hasSpecularMap;
	bool hasEmissiveMap;
	bool hasNormalMap;

	float shininess;
};

// Pour la lumiere du soleil
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};

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
	vec3 color;
	vec3 position;
	float intensity;
	Attenuation att;
};

// Les donnes relative au brouillard
struct Fog
{
	// Si il y a du brouillard ( 0 ou 1 )
	bool activeFog;
	vec3 colour; // sa couleur
	float density; // son facteur de densité 
};

// Material information
uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform Material  material;

// Shadow map texture
uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;

uniform mat4 invProjectionMatrix;

// Shadow information
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4  orthoProjectionMatrix[NUM_CASCADES];
uniform bool  renderShadow;

uniform DirectionalLight directionalLight;

uniform PointLight pointLights[MAX_POINT_LIGHTS];

uniform Fog fog;

void testAlpha(in vec4 diffuseColor)
{
	if(diffuseColor.w == 0)
	{
		discard;
	}
}

vec3 depthToViewPos(in float depth, in vec2 texCoord) {
	float z = depth * 2.0 - 1.0;

	vec4 clipSpacePos = vec4(texCoord * 2.0 - 1.0, z, 1.0);
	vec4 viewSpacePos = invProjectionMatrix * clipSpacePos;

    // Perspective division
    viewSpacePos /= viewSpacePos.w;

    return viewSpacePos.xyz;
}

vec3 calcLightColour(in vec3 diffuseColor, in float specularFactor, in float shininess, in vec3 lightColor, in vec3 toLightDir, in vec3 normal, in vec3 camDir)
{
	// Les deux type de lumiere
	vec3 specular = vec3(0);
	vec3 diffuse = vec3(0);

    // La lumiere diffusé

	// Moins un objet est orienté vers la source de lumiere moins il est éclairé, le facteur de diffusion
	// est donc calculer par l'ecart entre le direction de la source de lumiere et la tangente du point.
	float diffuseAngle = dot(toLightDir, normal);

	// On evite les calcul inutiles si l'angle est inferieur ou egale a zero
	if(diffuseAngle > 0) {
		// La lumière diffusé de l'objet est le produit de la couleur de l'objet multiplie par la couleur
		// de la source de lumiere multiplier par l'angle entre la normal et la source
		diffuse = lightColor * diffuseColor * diffuseAngle;
	}

	// La lumière spéculer ( l'effet de surbrillance )
	// On calcul le rayon reflechie au rayon incident de la lumiere, c'est a dire que l'angle entre le rayon incident
	// de la lumiere et la normal (theta) et le même qu'entre le rayon réflechie et la normal.
	vec3 reflectDir = reflect( -toLightDir, normal);
	
	// On calcul omega, l'angle entre le rayon reflechie et la camera
	float specularAngle = dot(reflectDir, camDir);

	// On evite les calcul inutiles si l'angle est inferieur ou egale a zero
	if(specularAngle > 0) {
		// Omega est mis a la puissance en fonction de la reflectance du material
		specularAngle = pow(specularAngle, shininess);

		// La lumiere spéculer est donc : la lumiere de la source de lumiere multiplie
		// par la lumiere spéculer de l'objet multiplir par le facteur
		specular = lightColor * specularFactor * specularAngle;
	}

	// La couleur finale est l'addition des la lumiere diffusé et spéculer
	return (diffuse + specular);
}

vec3 calcDirectionalLight(in vec3 diffuseColor, in float specularFactor, in float shininess, in DirectionalLight light, in vec3 normal, in vec3 camDir)
{
	return calcLightColour(diffuseColor, specularFactor, shininess, light.color, light.direction, normal, camDir);
}

// Calcule comment le point est illuminé en fonction de sa position dans l'espace
vec3 calcPointLight(in vec3 diffuseColor, in float specularFactor, in float shininess, in PointLight light, in vec3 position, in vec3 normal, in vec3 camDir)
{
	vec3 lightDir = light.position - position;
    vec3 toLightDir  = normalize(lightDir); // On normalise ce vecteur
    vec3 lightColor = calcLightColour(diffuseColor, specularFactor, shininess, light.color, toLightDir, normal, camDir);

    // L'attenuation de la lumière en fonction de la distance
	float distance = length(lightDir); // La distance entre la source de lumière est le point a illuminer
	// Le facteur d'attenuation de couleur est définie par la formule mathématique suivante : 
	float attenuationInv = 1 / (light.att.constant + light.att.linear * distance + light.att.exponent * (distance * distance));

	// float attenuationInv = distance * ( light.att.constant  + light.att.linear + light.att.exponent * distance);

    // La couleur renvoyé est donc la lumiere diffuse plus la lumière retourné a notre oeil, le tout diviser par le facteur d'attenuation
    return lightColor * attenuationInv;
}

vec3 calcFog(in vec3 pos, in vec3 colour, in Fog fog, in DirectionalLight directionalLight)
{
	float distance = length(pos);

    // Fromule mathématique pour la facteur de brouillard : 1 / e(distance * density)^2
    // Plus on est loin plus on a l'impression qu'il y a de brouillard
    float fogFactor = 1.0 / exp( pow(distance * fog.density, 2));

    fogFactor = clamp( fogFactor, 0.0, 1.0 ); // On force le facteur a être entre 0 et 1

	// On calcul la lumiere du brouillard en tenant compte de la lumiere ambiante, de la lumiere du "soleil" et de l'intensité du "soleil".
	vec3 fogColour = fog.colour * directionalLight.color;

	// La couleur finale est donc l'interpolation linéaire entre la couleur du brouillard et la couleur du fragment par le poids fogFactor
	vec3 resultColour = mix( fogColour, colour, fogFactor );

	return resultColour; // On préserve alpha ( w )
}

void main()
{
	fs_color = texture(diffuseMap, vs_textCoord);
	testAlpha(fs_color);

	if(!renderShadow)
	{
    // Information de base sur le pixel
    vec4 diffuseC;
    float specularFactor = material.specularFactor;
    vec3 normal = vs_mvNormal;

    if(material.hasDiffuseMap)
    {
        // On recupere la couleur et on test la transparence
        diffuseC = texture(diffuseMap, vs_textCoord);
        testAlpha(diffuseC);

        if(material.hasNormalMap)
        {
            // On recupere la normal
            vec4 norm = texture(normalMap, vs_textCoord);

            // On convertie de RGB space [0 - 1] en local space [-1 - 1] : 
            normal = vs_normalMatrix * (norm.xyz * 2 - 1);

            if(material.hasSpecularMap)
            {
            	specularFactor = norm.w;
            }
        }
    }
    else
    {
    	diffuseC = material.diffuse;
    	testAlpha(diffuseC);
    }

    float shadowFactor = 1;
    if(renderShadow)
    {
        // J'ai fait expres de ne pas faire de boucle, car un break est tres lourd pour la carte graphique.
        // Les conditions sont aussi plus rapides donc c worse ^^
        float depth = abs(vs_mvDepth);
        if(depth < cascadeFarPlanes[0])
        {
        	vec3 projCoord = vs_mlightviewVertexPos[0].xyz;

        	float depthT = projCoord.z - SHADOW_BIAS;
        	float textDepth = texture(shadowMap_0, projCoord.xy).r;

        	if(depthT > textDepth) {
        		shadowFactor = 0.1;
        	}
        } 
        else if (depth < cascadeFarPlanes[1])
        {
        	vec3 projCoord = vs_mlightviewVertexPos[1].xyz;

        	float depthT = projCoord.z - SHADOW_BIAS;
        	float textDepth = texture(shadowMap_1, projCoord.xy).r;

        	if(depthT > textDepth) {
        		shadowFactor = 0.1;
        	}
        }
        else if(depth < cascadeFarPlanes[2])
        {
        	vec3 projCoord = vs_mlightviewVertexPos[2].xyz;

        	float depthT = projCoord.z - SHADOW_BIAS;
        	float textDepth = texture(shadowMap_2, projCoord.xy).r;

        	if(depthT > textDepth) {
        		shadowFactor = 0.1;
        	}
        }
    }

    vec3 viewPos = depthToViewPos(vs_mvDepth, vs_textCoord);

	// On l'avais passé entre 1 et 0 vue qu'on a 8 bit (256) en float donc on remultiplie par 256
	float shininess = material.shininess * 256;

	// La camera est placé a l'origine
	vec3 camera_direction = normalize(-viewPos);

	// On récupere la lumière en fonction de la position du point
	vec3 diffuseSpecularComp = calcDirectionalLight(diffuseC.xyz, specularFactor, shininess, directionalLight, normal, camera_direction) * shadowFactor;

	for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
	    // Comment savoir lorsqu'un element de la liste est present ou non ? On verifie son intensite, si elle est a 0, on saute l'element
	    if ( pointLights[i].intensity > 0) 
	    {
	    	if(diffuseSpecularComp.x >= 1 && diffuseSpecularComp.y >= 1 && diffuseSpecularComp.z >= 1) {
	    		break;
	    	}

	    	diffuseSpecularComp += calcPointLight(diffuseC.xyz, specularFactor, shininess, pointLights[i], viewPos, normal, camera_direction);
	    }
	    else
	    {
	    	break;
	    }
	}

	fs_color = vec4(clamp(diffuseSpecularComp, 0, 1), diffuseC.w);

	if ( fog.activeFog ) {
		fs_color = vec4(calcFog(viewPos, fs_color.xyz, fog, directionalLight), 1);
	}
}
}
