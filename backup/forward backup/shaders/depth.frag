#version 330

uniform sampler2D texture_sampler;

in vec2 vs_texCoord;

void main()
{
	// On test la transparence, si le fragment est transparent, on le supprime
	if (texture(texture_sampler, vs_texCoord).w == 0) {
		discard;
	}

    //gl_FragDepth = gl_FragCoord.z; C'est ce que fait de base OpenGL, cette ligne est juste explicative
}
