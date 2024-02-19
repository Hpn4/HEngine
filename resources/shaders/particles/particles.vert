#version 330

const vec4 position = vec4(1);

layout (location=0) in mat4  modelViewMatrix;
layout (location=4) in float scale;

uniform mat4 projectionMatrix;

out mat4  vs_projectionMatrix;
out float vs_scale;

void main()
{
	gl_Position = modelViewMatrix * position;

    vs_projectionMatrix = projectionMatrix;
    vs_scale = scale;
}