#version 330

uniform sampler2D diffuseMap;

uniform bool useColor;

in vec2 gs_texCoord;
in vec3 vs_color;

layout (location = 0) out vec4 fs_color;
layout (location = 1) out vec4 fs_emissive;

void main()
{
	if(useColor) {
		float alpha = texture(diffuseMap, gs_texCoord).a;
		fs_color = vec4(vec3(1) * vs_color, alpha);
	} else {
		fs_color = texture(diffuseMap, gs_texCoord);
	}

	fs_emissive = vec4(0, 0, 0, 1);
}