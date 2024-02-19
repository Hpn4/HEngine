#version 330

uniform sampler2D diffuseMap;

in vec2 vs_texCoord;

void main()
{
	// On test la transparence, si le fragment est transparent, on le supprime
	if(texture(diffuseMap, vs_texCoord).a == 0) {
		discard;
	}
    //gl_FragDepth = gl_FragCoord.z; C'est ce que fait de base OpenGL, cette ligne est juste explicative
}
