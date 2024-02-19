#version 330

#define NUM_CASCADES 3
#define SHADOW_RANGE_PCF 1
#define SHADOW_BIAS 0.005;

uniform vec2  SHADOW_INC_MAP_0 = 1.0 / vec2(2048, 2048) * 0.5;

in vec2  vs_textCoord;
in vec3  vs_mvNormal;
in float vs_mvDepth;
in vec4  vs_mlightviewVertexPos[NUM_CASCADES];
in mat3  vs_normalMatrix;

layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec3 fs_specular;
layout (location = 3) out vec3 fs_normal;
layout (location = 4) out vec3 fs_shadow;

struct Material
{
    vec4 diffuse;
    vec4 specular;
    vec4 emissive;

    bool hasDiffuseMap;
    bool hasSpecularMap;
    bool hasEmissiveMap;
    bool hasNormalMap;

    float reflectance;
};

// Material information
uniform sampler2D diffuseMap;
uniform sampler2D specularMap;
uniform sampler2D emissiveMap;
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

vec3 calcNormal()
{
    if ( material.hasNormalMap )
    {
        vec3 newNormal = texture(normalMap, vs_textCoord).rgb;
        newNormal = normalize(newNormal * 2 - 1);

        return normalize(vs_normalMatrix * newNormal);
    }

    return vs_mvNormal;
}

float calcShadow(in vec4 position, in int idx)
{
    if ( !renderShadow ) {
        return 1.0;
    }

    if( position.z > 1.0) {
        return 0;
    }

    vec3 projCoord = position.xyz * 0.5 + 0.5;

    int shadowFactor = 0;
    float depth = projCoord.z - SHADOW_BIAS;

    for(int row = -SHADOW_RANGE_PCF; row <= SHADOW_RANGE_PCF; ++row)
    {
        for(int col = -SHADOW_RANGE_PCF; col <= SHADOW_RANGE_PCF; ++col)
        {
            float textDepth;
            vec2 finalPos = projCoord.xy + vec2(row, col) * SHADOW_INC_MAP_0;
            if ( idx == 0) 
            {
                textDepth = texture(shadowMap_0, finalPos).r;
            }
            else if ( idx == 1)
            {
                textDepth = texture(shadowMap_1, finalPos).r;   
            }
            else
            {
                textDepth = texture(shadowMap_2, finalPos).r;
            }

            if(depth > textDepth) {
                shadowFactor++;
            }
        }
    }

    return 1 - (shadowFactor * 0.1);
}

void main()
{
    vec3 emissiveC;
    if ( material.hasEmissiveMap ) {
        emissiveC = texture(emissiveMap, vs_textCoord).xyz;
    } else {
        emissiveC = material.emissive.xyz;
    }

    if(emissiveC == vec3(0))
    {
        vec4 diffuseC;
        vec3 specularC;

        if ( material.hasDiffuseMap ) {
            diffuseC = texture(diffuseMap, vs_textCoord);
        } else {
            diffuseC = material.diffuse;
        }

        if ( material.hasSpecularMap ) {
            specularC = texture(specularMap, vs_textCoord).xyz;
        } else {
            specularC = material.specular.xyz;
        }

        if(diffuseC.w == 0) {
            discard;
        }

        fs_diffuse  = diffuseC.xyz;
        fs_specular = specularC;
        fs_normal   = calcNormal();

        float depth = abs(vs_mvDepth);
        for (int i = 0; i < NUM_CASCADES; i++)
        {
            if ( depth < cascadeFarPlanes[i] )
            {
                fs_shadow  = vec3(calcShadow(vs_mlightviewVertexPos[i], i), material.reflectance, 0);
                return;
            }
        }
    }
    else
    {
        fs_diffuse = emissiveC;
        fs_shadow = vec3(0, 0, 1);
    }
}