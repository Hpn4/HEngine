#version 330

uniform sampler2D diffuseMap;

in vec2 gs_texCoord;

out vec4 fs_color;

void main()
{
	fs_color = texture(diffuseMap, gs_texCoord); // On applique la texture basiquement
}