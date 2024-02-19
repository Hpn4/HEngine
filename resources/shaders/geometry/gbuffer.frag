#version 330

#define NUM_CASCADES 3
#define SHADOW_BIAS 0.005

in vec2  vs_textCoord;
in vec3  vs_mvNormal;
in float vs_mvDepth;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat3  vs_normalMatrix;

layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec4 fs_normal;
layout (location = 3) out vec3 fs_shadow;

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

// Material information
uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform Material  material;

// Shadow map texture
uniform sampler2D shadowMap_0;
uniform sampler2D shadowMap_1;
uniform sampler2D shadowMap_2;

// Shadow information
uniform float cascadeFarPlanes[NUM_CASCADES];
uniform mat4  orthoProjectionMatrix[NUM_CASCADES];
uniform bool  renderShadow;

void main()
{
    // Information de base sur le pixel
    vec4 diffuseC;
    float specularFactor = material.specularFactor;
    bool isEmissive = false;
    vec3 normal = vs_mvNormal;

    if(material.hasDiffuseMap)
    {
        // On recupere la couleur et on test la transparence
        diffuseC = texture(diffuseMap, vs_textCoord);

        if(material.hasNormalMap)
        {
            // On recupere la normal
            vec4 norm = texture(normalMap, vs_textCoord);

            // On convertie de RGB space [0 - 1] en local space [-1 - 1] : 
            normal = vs_normalMatrix * (norm.xyz * 2 - 1);

            if(material.hasEmissiveMap)
            {
                if(material.hasSpecularMap)
                {
                    if(!(isEmissive = norm.w == 0))
                    {
                        specularFactor = norm.w;
                    }
                }
                else
                {
                    isEmissive = norm.w == 1;
                }
            }
            else if(material.hasSpecularMap)
            {
            	if(material.emissive != vec3(0)) {
            		diffuseC = vec4(material.emissive, 1);
            		isEmissive = true;
            	}

                specularFactor = norm.w;
            }
        }
    }
    else
    {
        diffuseC = material.diffuse;
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

    fs_diffuse = diffuseC.xyz;
    fs_normal = vec4(normal, isEmissive);

    fs_shadow = vec3(shadowFactor, material.shininess, specularFactor);
}