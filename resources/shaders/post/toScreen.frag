#version 330

const vec3 GAMMA = vec3(1/2.2);

uniform sampler2D sceneTex;

// Bloom texture
uniform sampler2D firstBloomTex;
uniform sampler2D secondBloomTex;
uniform sampler2D thirdBloomTex;

uniform bool applyExposure;
uniform float exposure;

in vec2 vs_texCoord;

out vec4 fs_color;

void main() {
    vec3 sceneColor = texture(sceneTex, vs_texCoord).xyz;

    // Bloom color
    vec3 firstBloomColor = texture(firstBloomTex, vs_texCoord).xyz;
    vec3 secondBloomColor = texture(secondBloomTex, vs_texCoord).xyz;
    vec3 thirdBloomColor = texture(thirdBloomTex, vs_texCoord).xyz;

    sceneColor += (firstBloomColor + secondBloomColor + thirdBloomColor);

    if(applyExposure) {
    	vec3 result = vec3(1.0) - exp(-sceneColor * exposure);

    	fs_color = vec4(pow(result, GAMMA), 1.0);
    } else {
    	fs_color = vec4(pow(sceneColor, GAMMA), 1.0);
    }
}