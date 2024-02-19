#version 330

const float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

uniform bool horizontal;
uniform sampler2D sceneTex;

uniform vec2 screenSize;

out vec4 fs_color;

vec2 getTextCoord()
{
	return gl_FragCoord.xy / screenSize;
}

void main() 
{
	vec2 texCoord = getTextCoord();
    vec3 result = texture(sceneTex, texCoord).rgb * weight[0]; // current fragment's contribution

    vec2 tex_offset = 1.0 / textureSize(sceneTex, 0); // gets size of single texel

    if(horizontal)
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(sceneTex, texCoord + vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
            result += texture(sceneTex, texCoord - vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(sceneTex, texCoord + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
            result += texture(sceneTex, texCoord - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
        }
    }

    fs_color = vec4(result, 1.0);
}