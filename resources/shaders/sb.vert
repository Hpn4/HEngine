#version 330

layout (location = 0) in vec3 position;

uniform mat4 projModelViewMatrix;

out vec3 vs_texCoord;

void main()
{
    gl_Position = projModelViewMatrix * vec4(position, 1.0);

    vs_texCoord = position;
}