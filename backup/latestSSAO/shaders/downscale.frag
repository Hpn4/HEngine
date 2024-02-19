#version 330

uniform sampler2D texture_sampler;
uniform vec2 texOff;

in vec2 vs_texCoord;

out vec3 fs_color;

void main()
{
	fs_color = texture(texture_sampler, vs_texCoord).xyz * 0.25; // Top left point
	fs_color += texture(texture_sampler, vs_texCoord + vec2(1, 0) * texOff).xyz * 0.25; // Top right point
	fs_color += texture(texture_sampler, vs_texCoord + vec2(0, 1) * texOff).xyz * 0.25; // Bottom left point
	fs_color += texture(texture_sampler, vs_texCoord + vec2(1, 1) * texOff).xyz * 0.25; // Bottom right point
}