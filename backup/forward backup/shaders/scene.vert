#version 330

const int NUM_CASCADES = 3;

layout (location=0)  in vec3  position;
layout (location=1)  in vec2  texCoord;
layout (location=2)  in vec3  vertexNormal;
layout (location=3)  in mat4  modelInstancedMatrix;
layout (location=7)  in float selectedInstanced;

uniform bool  isInstanced;
uniform mat4  viewMatrix;
uniform mat4  projectionMatrix;
uniform mat4  modelNonInstancedMatrix;
uniform mat4  lightViewMatrix[NUM_CASCADES];
uniform mat4  orthoProjectionMatrix[NUM_CASCADES];
uniform float selectedNonInstanced;

out vec2  vs_textCoord;
out vec3  vs_mvNormal;
out vec3  vs_mvVertexPos;
out vec4  vs_mlightviewVertexPos[NUM_CASCADES];
out mat4  vs_modelViewMatrix;
out float vs_selected;

// Precalcul de lumiere
out vec3  vs_cameraDirection;

void preLightCalculation()
{
    vs_cameraDirection = normalize(-vs_mvVertexPos);
}

void main()
{
    vec4 pos = vec4(position, 1.0);

    mat4 modelMatrix;

    if ( isInstanced ) 
    {
        vs_selected = selectedInstanced;
        modelMatrix = modelInstancedMatrix;
    }
    else
    {
        vs_selected = selectedNonInstanced;
        modelMatrix = modelNonInstancedMatrix;
    }

    mat4 modelViewMatrix = viewMatrix * modelMatrix;  
	// Position dans le monde (la dimensions w : 1.0 sert pour les transformations)
	vec4 mvPos = modelViewMatrix * pos;

	// Position de projection, adapte au FOV, a la taille de la fenetre...
	gl_Position = projectionMatrix * mvPos;

    // Support for texture atlas, update texture coordinates
    vs_textCoord = texCoord;

    // Le vertex normal pour la lumiere, diemsnion w : 0 pour empecher translation
    // Le vertex normal indique la tangente du point
    vs_mvNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;

    // Le vertex de position est égale a la position dans le monde
    vs_mvVertexPos = mvPos.zyz;

    for ( int i = 0; i < NUM_CASCADES; i++ ) {
        vs_mlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * pos;
    }

    vs_modelViewMatrix = modelViewMatrix;

    preLightCalculation();
}