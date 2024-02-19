#version 330

const vec4 position = vec4(1, 1, 1, 1);

layout (location=0) in vec2  texOffset;
layout (location=1) in mat4  modelMatrix;
layout (location=5) in float scale;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out mat4  vs_projectionMatrix;
out float vs_scale;

void main()
{
	mat4 modelViewMatrix = viewMatrix * modelMatrix;

    // On rédéfinie la matrice pour le scale
	modelViewMatrix[0][0] = scale;
	modelViewMatrix[1][1] = scale;
	modelViewMatrix[2][2] = scale;

	gl_Position = modelViewMatrix * position;

    vs_projectionMatrix = projectionMatrix;
    vs_scale = scale;
}