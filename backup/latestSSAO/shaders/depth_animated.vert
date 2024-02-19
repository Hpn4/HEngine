#version 330

const int  MAX_WEIGHTS = 4;
const int  MAX_JOINTS  = 150;
const vec4 zero        = vec4(0); 

layout (location=0) in vec3  position;
layout (location=3) in vec4  jointWeights;
layout (location=4) in ivec4 jointIndices;

uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 orthoProjLightViewModelMatrix;

void main()
{
	vec4 defaultPos = vec4(position, 1.0);
	vec4 initPos = zero;

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
		}
	}

	gl_Position = orthoProjLightViewModelMatrix * initPos;
}
