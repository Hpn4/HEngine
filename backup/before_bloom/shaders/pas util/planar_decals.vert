#version 330

layout ( location = 0 ) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

uniform sampler2D depthTex;

uniform mat4 invProjectionMatrix;
uniform mat4 invViewMatrix;
uniform mat4 invModelMatrix;

uniform vec2 screenSize;

out vec3 vs_localPos;

// this is supposed to get the world position from the depth buffer
vec3 depthToWorldPos(in float depth, in vec2 texCoord) {
    float z = depth * 2.0 - 1.0;

    vec4 clipSpacePos = vec4(texCoord * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePos = invProjectionMatrix * clipSpacePos;

    // Perspective division
    viewSpacePos /= viewSpacePos.w;

    vec4 worldSpacePos = invViewMatrix * viewSpacePos;

    return worldSpacePos.xyz;
}

void main()
{
	vec4 posCS = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1);

    vec2 screenPosition = posCS.xy / posCS.w;

    vec2 depthUV = screenPosition * 0.5f + 0.5f;

    depthUV += vec2(0.5f / screenSize.x, 0.5f / screenSize.y); //half pixel offset

    float depth = texture(depthTex, depthUV).r;

    vec3 worldPos = depthToWorldPos(depth, depthUV);

    vec3 localPos = (invModelMatrix * vec4(worldPos, 1)).xyz;

    vec3 test = 0.5 - abs(localPos);

    vs_localPos = localPos;

    gl_Position = posCS;
}