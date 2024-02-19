#version 330

layout ( location = 0 ) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

out vec4 vs_posCS; // clip space

void main()
{
    vs_posCS = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);

    gl_Position = vs_posCS;
}