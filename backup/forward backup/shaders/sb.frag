#version 330

uniform samplerCube texture_sampler;
uniform samplerCube secondSky;

uniform float blendFactor;

in vec3 vs_texCoord;

out vec4 fs_color;

void main()
{
    vec4 firstColor = texture(texture_sampler, vs_texCoord);
    vec4 secondColor = texture(secondSky, vs_texCoord);

    fs_color = mix(firstColor, secondColor, blendFactor);
}