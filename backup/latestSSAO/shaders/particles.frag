#version 330

uniform sampler2D diffuseMap;

in vec2 gs_texCoord;

layout (location = 0) out vec4 fs_color;
layout (location = 1) out vec4 fs_emissive;

void main()
{
	fs_color = texture(diffuseMap, gs_texCoord); // On applique la texture basiquement
	fs_emissive = vec4(0, 0, 0, 1);
}