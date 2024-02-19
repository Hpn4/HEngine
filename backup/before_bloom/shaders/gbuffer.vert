#version 330

const int NUM_CASCADES = 3;

layout (location=0)  in vec3 position;
layout (location=1)  in vec2 texCoord;
layout (location=2)  in vec3 vertexNormal;
layout (location=3)  in mat4 modelInstancedMatrix;
layout (location=7)  in mat3 normalInstancedMatrix;

uniform bool isInstanced;
uniform mat4 projectionViewMatrix;
uniform mat4 modelNonInstancedMatrix;
uniform mat3 normalNonInstancedMatrix;

uniform mat4 orthoProjLightViewMatrix[NUM_CASCADES];

out vec2  vs_textCoord;
out vec3  vs_mvNormal;
out vec4  vs_mlightviewVertexPos[NUM_CASCADES];
out mat3  vs_normalMatrix;
out float vs_mvDepth;

void main()
{
    vec4 pos = vec4(position, 1.0);

    mat4 modelMatrix;
    mat3 normalMatrix;

    if ( isInstanced ) 
    {
        modelMatrix = modelInstancedMatrix;
        normalMatrix = normalInstancedMatrix;
    }
    else
    {
        modelMatrix = modelNonInstancedMatrix;
        normalMatrix = normalNonInstancedMatrix;
    }

	// Clip space coord systeme
	gl_Position = projectionViewMatrix * modelMatrix * pos;

    // On transferrt les coord de texture
    vs_textCoord = texCoord;

    // Le vertex normal indique la tangente du point, sert pour les calculs de lumiere
    vs_mvNormal = normalize(normalMatrix * vertexNormal);

    vs_mvDepth = gl_Position.z;

    for ( int i = 0; i < NUM_CASCADES; i++ ) {
        vs_mlightviewVertexPos[i] = orthoProjLightViewMatrix[i] * modelMatrix * pos;
    }

    vs_normalMatrix = normalMatrix;
}