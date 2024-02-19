#version 330

layout (location = 1) out vec3 fs_diffuse;
layout (location = 2) out vec4 fs_normal;

uniform vec3 lightColor;

void main()
{
   fs_diffuse = lightColor;
   fs_normal.w = 1;
}