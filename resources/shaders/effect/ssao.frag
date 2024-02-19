#version 330

const int kernelSize = 64;
const float radius = 1;
const float bias = 0.001;

uniform sampler2D depthText; 
uniform sampler2D normalsText;
uniform sampler2D noiseText;

uniform vec3 samples[64];
uniform mat4 projectionMatrix;
uniform mat4 invProjectionMatrix;
uniform vec2 noiseScale; 

in vec2 vs_texCoord;

out float fs_color;

vec3 depthToViewPos(in vec2 texCoord) {
	float depth = texture(depthText, texCoord).r;
	float z = depth * 2.0 - 1.0;

	vec4 clipSpacePos = vec4(texCoord * 2.0 - 1.0, z, 1.0);
	vec4 viewSpacePos = invProjectionMatrix * clipSpacePos;

    // Perspective division
    viewSpacePos /= viewSpacePos.w;

    return viewSpacePos.xyz;
}

void main()
{
	// On recupere la position du fragment
	vec3 origin = depthToViewPos(vs_texCoord);

    vec3 normal = texture(normalsText, vs_texCoord).xyz * 2.0 - 1.0;
    normal = normalize(normal);

    vec3 randomVec = texture(noiseText, vs_texCoord * noiseScale).xyz * 2.0 - 1.0;
    
    // create TBN change-of-basis matrix: from tangent-space to view-space
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);

    // iterate over the sample kernel and calculate occlusion factor
    float occlusion = 0.0;
    for(int i = 0; i < kernelSize; i++)
    {
        // On recupere la position du samples a partir des valeurs aleatoirs pregeneres
        vec3 samplePos = TBN * samples[i]; // from tangent to view-space
        samplePos = samplePos * radius + origin;
        
        // project sample position (to sample texture) (to get position on screen/texture)
        vec4 offset = vec4(samplePos, 1.0);
        offset = projectionMatrix * offset; // from view to clip-space
        offset.xy /= offset.w; // perspective divide
        offset.xy = offset.xy * 0.5 + 0.5; // transform to range 0.0 - 1.0
        
        // On recupere la pronfondeur du fragment
        float sampleDepth = depthToViewPos(offset.xy).z;
        
        // On verifie que le samples est pas trop eloignes
        float rangeCheck = smoothstep(0.0, 1.0, radius / abs(origin.z - sampleDepth));
        occlusion += (sampleDepth >= samplePos.z + bias ? 1.0 : 0.0) * rangeCheck;
    }
    occlusion = 1.0 - (occlusion / kernelSize); 

    fs_color = occlusion;
}