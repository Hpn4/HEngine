#version 330

layout (location = 1) out vec3 fs_diffuse;
layout (location = 4) out vec3 fs_shadow;

uniform vec3 lightColor;

void main()
{
   fs_diffuse = lightColor;
   fs_shadow = vec3(0, 0, 1);
}