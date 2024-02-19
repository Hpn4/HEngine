#version 330

layout ( location = 0 ) in vec3 position;

uniform mat4 projViewModelMatrix;

void main()
{
    gl_Position = projViewModelMatrix * vec4(position, 1);
}