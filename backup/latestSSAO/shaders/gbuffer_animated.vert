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

uniform mat4 projectionViewMatrix;
uniform mat4 modelMatrix;
uniform mat3 normalMatrix;

uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 orthoProjLightViewMatrix[NUM_CASCADES];

out vec2  vs_textCoord;
out vec3  vs_mvNormal;
out vec4  vs_mlightviewVertexPos[NUM_CASCADES];
out mat3  vs_normalMatrix;
out float vs_mvDepth;

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

	// Clip space coord systeme
	gl_Position = projectionViewMatrix * modelMatrix * initPos;

    // Coordonne pour les textures
    vs_textCoord = texCoord;

    // Le vertex normal indique la tangente du point, sert pour la lumiere
    vs_mvNormal = normalize(normalMatrix * initNormal.xyz);

    vs_mvDepth = gl_Position.z;

    // Les différente projection orthographiques pour les ombres
    for ( int i = 0; i < NUM_CASCADES; i++ ) {
        vs_mlightviewVertexPos[i] = (orthoProjLightViewMatrix[i] * modelMatrix * defaultPos) * 0.5 + 0.5;
    }

    // La matrix de vision du modele
    vs_normalMatrix = normalMatrix;
}