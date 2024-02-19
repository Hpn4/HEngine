#version 330

layout (location=0) in vec3  position;
layout (location=1) in vec2  texCoord;
layout (location=2) in vec3  vertexNormal;
layout (location=3) in mat4  modelInstancedMatrix;

uniform bool isInstanced;
uniform mat4 modelNonInstancedMatrix;
uniform mat4 lightViewMatrix;
uniform mat4 orthoProjectionMatrix;

out vec2 vs_texCoord;

void main()
{
	mat4 modelMatrix = isInstanced ? modelInstancedMatrix : modelNonInstancedMatrix;

	gl_Position = orthoProjectionMatrix * lightViewMatrix * modelMatrix * vec4(position, 1.0);

	vs_texCoord = texCoord;
}
