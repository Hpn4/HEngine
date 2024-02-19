#version 330

const float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

uniform bool horizontal;
uniform sampler2D tex;

uniform vec2 texOff;

in vec2 vs_texCoord;

out vec4 fs_color;

void main() 
{
    vec3 result = texture(tex, vs_texCoord).xyz * weight[0]; // current fragment's contribution

    if(horizontal)
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(tex, vs_texCoord + vec2(texOff.x * i, 0.0)).xyz * weight[i];
            result += texture(tex, vs_texCoord - vec2(texOff.x * i, 0.0)).xyz * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 5; ++i)
        {
            result += texture(tex, vs_texCoord + vec2(0.0, texOff.y * i)).xyz * weight[i];
            result += texture(tex, vs_texCoord - vec2(0.0, texOff.y * i)).xyz * weight[i];
        }
    }

    fs_color = vec4(result, 1.0);
}