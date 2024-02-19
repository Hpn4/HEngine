#version 330

uniform samplerCube texture_sampler;
uniform samplerCube secondSky;

uniform sampler2D depthText;

uniform vec3 ambientLight;
uniform vec2 screenSize;
uniform float blendFactor;

in vec3 vs_texCoord;

out vec4 fs_color;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

void main()
{
	vec2 textCoord = getTextCoord();
	float depth = texture(depthText, textCoord).r;

	if( depth == 1)
	{
		vec4 firstColor = texture(texture_sampler, vs_texCoord);
		vec4 secondColor = texture(secondSky, vs_texCoord);

		fs_color = vec4(ambientLight, 1) * mix(firstColor, secondColor, blendFactor);
	}
	else 
	{
		discard;
	}
}