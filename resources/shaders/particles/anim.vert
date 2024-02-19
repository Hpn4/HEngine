#version 330

layout (location=0) in vec3  position;
layout (location=1) in vec2  texCoord;
layout (location=2) in mat4  modelViewMatrix;
layout (location=6) in float scale;
layout (location=7) in vec2  texOffset;
layout (location=8) in float ttlRatio;

out vec2 gs_texCoord;
out vec3 vs_color;

uniform mat4 projectionMatrix;

uniform vec3 colorStart;
uniform vec3 colorFactor;

uniform vec2 texCoordOff[4];

uniform bool useColor;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    
    // On met à jour les textures pour les atlas
    vec2 off = texCoordOff[gl_VertexID];
    gs_texCoord = vec2(off.x + texOffset.x, off.y + texOffset.y);

    if(useColor) {
    	vs_color = colorStart + colorFactor * ttlRatio;
    }
}