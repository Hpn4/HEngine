#version 330

layout (location=0) in vec3 position;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec3 vs_texCoord;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    //gl_Position = projectionMatrix * vec4(position, 1.0);
    vs_texCoord = position;
}