#version 330

const vec3 GAMMA = vec3(1/2.2);

uniform sampler2D sceneTex;
uniform sampler2D bloomBlurTex;

uniform float exposure;

uniform vec2 screenSize;
uniform vec2 bloomSize;

out vec4 fs_color;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

void main() {
	vec2 texCoord = getTextCoord();

    vec3 sceneColor = texture(sceneTex, texCoord).xyz;
    vec3 bloomColor = texture(bloomBlurTex, texCoord).xyz;

    sceneColor += bloomColor;

    vec3 result = vec3(1.0) - exp(-sceneColor * exposure);

    fs_color = vec4(pow(result, GAMMA), 1.0);
}