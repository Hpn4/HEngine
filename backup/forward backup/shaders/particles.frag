#version 330

uniform sampler2D texture_sampler;

in vec2 gs_texCoord;

out vec4 fs_color;

void main()
{
	fs_color = texture(texture_sampler, gs_texCoord); // On applique la texture basiquement
}