#version 330

layout ( location = 1 ) out vec3 diffuse;

uniform sampler2D diffuseTex;

in vec3 vs_localPos;

void main()
{
	vec3 test = 0.5 - abs(vs_localPos);

    if(test.x > 0 && test.y > 0 && test.z > 0)
    {
        vec2 uv = vs_localPos.xy + vec2(0.5f);
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
        discard;
    }
}