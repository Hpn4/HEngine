#version 330

layout ( location = 1 ) out vec3 diffuse;

uniform sampler2D depthTex;
uniform sampler2D diffuseTex;

uniform mat4 invProjectionMatrix;
uniform mat4 invViewMatrix;
uniform mat4 invModelMatrix;

uniform vec2 halfPixelOff;

in vec4 vs_posCS;

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
    vec2 screenPosition = vs_posCS.xy / vs_posCS.w;

    vec2 depthUV = screenPosition * 0.5f + 0.5f;

    depthUV += halfPixelOff; //half pixel offset

    float depth = texture(depthTex, depthUV).r;

    vec3 worldPos = depthToWorldPos(depth, depthUV);
    vec4 localPos = invModelMatrix * vec4(worldPos, 1);

    vec3 test = 0.5 - abs(localPos.xyz);

    if(test.x > 0 && test.y > 0 && test.z > 0)
    {
        vec2 uv = localPos.xy + vec2(0.5f);
        vec4 diffuseColor = texture(diffuseTex, uv);
        if(diffuseColor.w == 0)
        {
            discard;
        }
        else
        {
            diffuse = diffuseColor.xyz;
        }
    }
    else
    {
        diffuse = vec3(1, 0, 0);
    }
}