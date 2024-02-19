#version 330

uniform sampler2D texture_sampler;

uniform vec2 screenSize;

out vec4 fs_color;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

void main() {
	vec2 texCoord = getTextCoord();

    fs_color = texture(texture_sampler, texCoord);
}