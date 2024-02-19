#version 330

const int MAX_WEIGHTS  = 4;
const int MAX_JOINTS   = 150;
const int NUM_CASCADES = 3;
const vec4 zero        = vec4(0);

layout (location=0)  in vec3  position;
layout (location=1)  in vec2  texCoord;
layout (location=2)  in vec3  vertexNormal;
layout (location=3)  in vec4  jointWeights;
layout (location=4)  in ivec4 jointIndices;

uniform mat4  viewMatrix;
uniform mat4  projectionMatrix;
uniform mat4  modelMatrix;
uniform mat4  jointsMatrix[MAX_JOINTS];
uniform mat4  lightViewMatrix[NUM_CASCADES];
uniform mat4  orthoProjectionMatrix[NUM_CASCADES];
uniform float selected;

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
	vec4 defaultPos    = vec4(position, 1.0);
	vec4 defaultNormal = vec4(vertexNormal, 0.0);

	vec4 initPos = zero;
	vec4 initNormal = zero;

	int count = 0;
	for ( int i = 0; i < MAX_WEIGHTS; i++) 
	{
		float weight = jointWeights[i];
		if(weight > 0) 
		{
			count++;
			int jointIndex = jointIndices[i];
			vec4 tmpPos = jointsMatrix[jointIndex] * defaultPos;
			initPos += weight * tmpPos;

			vec4 tmpNormal = jointsMatrix[jointIndex] * defaultNormal;
			initNormal += weight * tmpNormal;
		}
	}

	mat4 modelViewMatrix = viewMatrix * modelMatrix; 

	// Position dans le monde
	vec4 mvPos = modelViewMatrix * initPos;

	// Position de projection
	gl_Position = projectionMatrix * mvPos;

    // Coordonne pour les textures
    vs_textCoord = texCoord;

    // Le vertex normal pour la lumiere, diemsnion w : 0 pour empecher translation
    // Le vertex normal indique la tangente du point
    vs_mvNormal = normalize(modelViewMatrix * initNormal).xyz;

    // Le vertex de position est égale a la position dans le monde pas à celle de projection
    vs_mvVertexPos = mvPos.zyz;

    // Les différente projection orthographiques pour les ombres
    for ( int i = 0; i < NUM_CASCADES; i++ ) {
        vs_mlightviewVertexPos[i] = orthoProjectionMatrix[i] * lightViewMatrix[i] * modelMatrix * defaultPos;
    }

    // La matrix de vision du modele
    vs_modelViewMatrix = modelViewMatrix;

    // Si la figure est séléctionné
    vs_selected = selected;

    preLightCalculation();
}