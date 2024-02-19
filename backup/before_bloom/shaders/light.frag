#version 330

const int MAX_POINT_LIGHTS  = 100;
const int MAX_SPOT_LIGHTS   = 5;

in vec2 vs_texCoord;

out vec4 fs_color;

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

// Toute les textures
uniform sampler2D depthText; 
uniform sampler2D diffuseText;
uniform sampler2D specularText;
uniform sampler2D normalsText;
uniform sampler2D shadowText;

uniform mat4 invProjectionMatrix;

uniform DirectionalLight directionalLight;
uniform float specularPower;

uniform PointLight pointLights[MAX_POINT_LIGHTS];

uniform Fog fog;

vec3 depthToViewPos(in float depth, in vec2 texCoord) {
	float z = depth * 2.0 - 1.0;

	vec4 clipSpacePos = vec4(texCoord * 2.0 - 1.0, z, 1.0);
	vec4 viewSpacePos = invProjectionMatrix * clipSpacePos;

    // Perspective division
    viewSpacePos /= viewSpacePos.w;

    return viewSpacePos.xyz;
}

vec4 calcLightColour(in vec4 diffuseC, in vec4 speculrC, in float reflectance, in vec3 light_color, in vec3 to_light_dir, in vec3 normal, in vec3 camera_direction)
{
    // La lumiere diffusé

	// Moins un objet est orienté vers la source de lumiere moins il est éclairé, le facteur de diffusion
	// est donc calculer par l'ecart entre le direction de la source de lumiere et la tangente du point.
	float diffuseFactor = max( dot(normal, to_light_dir), 0.0); // Le résultat doit etre supérieur ou égale a 0

	// Enfin, la lumière diffusé est donc la lumiere diffusé normalement par l'objet (sa couleur), multiplié par la couleur 
	// de la source de lumiere multiplier par l'intensité de la lumiere multiplier par le facteur de diffusion
	vec4 light = vec4(light_color, 1.0);
	vec4 diffuseColour = diffuseC * light * diffuseFactor;

	// La lumière spéculer ( l'effet de surbrillance )
	vec3 reflected_light = normalize( reflect( -to_light_dir, normal));
	
	float specularFactor = max( dot(camera_direction, reflected_light), 0.0); // Le résultat doit etre supérieur a 0
	specularFactor = pow(specularFactor, specularPower);

	vec4 specColour = speculrC * specularFactor * reflectance * light;

	// La couleur renvoyé est donc la lumiere diffuse plus la lumière retourné a notre oeil
	return (diffuseColour + specColour);
}

vec4 calcDirectionalLight(in vec4 diffuseC, in vec4 speculrC, in float reflectance, in DirectionalLight light, in vec3 normal, in vec3 camera_direction)
{
	return calcLightColour(diffuseC, speculrC, reflectance, light.color, normalize(light.direction), normal, camera_direction);
}

// Calcule comment le point est illuminé en fonction de sa position dans l'espace
vec4 calcPointLight(in vec4 diffuseC, in vec4 speculrC, in float reflectance, in PointLight light, in vec3 position, in vec3 normal, in vec3 camera_direction)
{
	vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction); // On normalise ce vecteur
    vec4 light_colour = calcLightColour(diffuseC, speculrC, reflectance, light.color, to_light_dir, normal, camera_direction);

    // L'attenuation de la lumière en fonction de la distance
	float distance = length(light_direction); // La distance entre la source de lumière est le point a illuminer
	// Le facteur d'attenuation de couleur est définie par la formule mathématique suivante : 
	float attenuationInv = 1 / (light.att.constant + light.att.linear * distance + light.att.exponent * (distance * distance));

	// float attenuationInv = distance * ( light.att.constant  + light.att.linear + light.att.exponent * distance);

    // La couleur renvoyé est donc la lumiere diffuse plus la lumière retourné a notre oeil, le tout diviser par le facteur d'attenuation
    return light_colour * attenuationInv;
}

vec4 calcFog(in vec3 pos, in vec4 colour, in Fog fog, in DirectionalLight directionalLight)
{
	float distance = length(pos);

    // Fromule mathématique pour la facteur de brouillard : 1 / e(distance * density)^2
    // Plus on est loin plus on a l'impression qu'il y a de brouillard
    float fogFactor = 1.0 / exp( pow(distance * fog.density, 2));

    fogFactor = clamp( fogFactor, 0.0, 1.0 ); // On force le facteur a être entre 0 et 1

	// On calcul la lumiere du brouillard en tenant compte de la lumiere ambiante, de la lumiere du "soleil" et de l'intensité du "soleil".
	vec3 fogColour = fog.colour * directionalLight.color;

	// La couleur finale est donc l'interpolation linéaire entre la couleur du brouillard et la couleur du fragment par le poids fogFactor
	vec3 resultColour = mix( fogColour, colour.xyz, fogFactor );

	return vec4(resultColour, colour.w); // On préserve alpha ( w )
}

void main()
{
	vec3 shadow = texture(shadowText, vs_texCoord).xyz;
	vec4 diffuseC = texture(diffuseText, vs_texCoord);

	if( shadow.z == 0 )
	{
		float depth = texture(depthText, vs_texCoord).r;

		vec3 viewPos = depthToViewPos(depth, vs_texCoord);

		vec4 speculrC = texture(specularText, vs_texCoord);
		vec3 normal  = texture(normalsText, vs_texCoord).xyz;

		float shadowFactor = shadow.x;
		float reflectance = shadow.y;

		vec3 camera_direction = normalize(-viewPos);

		// On récupere la lumière en fonction de la position du point
		vec4 diffuseSpecularComp = calcDirectionalLight(diffuseC, speculrC, reflectance, directionalLight, normal, camera_direction);

		for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
			// Comment savoir lorsqu'un element de la liste est present ou non ? On verifie son intensite, si elle est a 0, on saute l'element
			if ( pointLights[i].intensity > 0) 
			{
				if(diffuseSpecularComp.x == 1 && diffuseSpecularComp.y == 1 && diffuseSpecularComp.z == 1) {
					break;
				}

				diffuseSpecularComp += calcPointLight(diffuseC, speculrC, reflectance, pointLights[i], viewPos, normal, camera_direction);
			//if (diffuseSpecularComp.xyz == vec3(1, 1, 1)) {
			//	break;
			//}
			}
			else
			{
				break;
			}
		}

		fs_color = clamp(diffuseSpecularComp * shadowFactor, 0, 1);

		if ( fog.activeFog ) {
			fs_color = calcFog(viewPos, fs_color, fog, directionalLight);
		}
	}
	else
	{
		fs_color = diffuseC;
	}
}