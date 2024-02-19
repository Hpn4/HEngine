#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=3) in mat4 orthoProjLightViewModelInstancedMatrix;

uniform bool isInstanced;
uniform mat4 orthoProjLightViewModelNonInstancedMatrix;

out vec2 vs_texCoord;

void main()
{
	mat4 orthoProjLightViewModelMatrix = isInstanced ? orthoProjLightViewModelInstancedMatrix : orthoProjLightViewModelNonInstancedMatrix;

	vs_texCoord = texCoord;
	gl_Position = orthoProjLightViewModelMatrix * vec4(position, 1.0);
}
