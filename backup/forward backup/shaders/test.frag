#version 330

uniform sampler2D texture_sampler;

out vec4 fs_color;

uniform vec2 screenSize;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

vec4 blur13(sampler2D image, vec2 uv, vec2 resolution, vec2 direction) {
  vec4 color = vec4(0.0);
  vec2 off1 = vec2(1.411764705882353) * direction;
  vec2 off2 = vec2(3.2941176470588234) * direction;
  vec2 off3 = vec2(5.176470588235294) * direction;
  color += texture(image, uv) * 0.1964825501511404;
  color += texture(image, uv + (off1 / resolution)) * 0.2969069646728344;
  color += texture(image, uv - (off1 / resolution)) * 0.2969069646728344;
  color += texture(image, uv + (off2 / resolution)) * 0.09447039785044732;
  color += texture(image, uv - (off2 / resolution)) * 0.09447039785044732;
  color += texture(image, uv + (off3 / resolution)) * 0.010381362401148057;
  color += texture(image, uv - (off3 / resolution)) * 0.010381362401148057;
  return color;
}

void main() {
	vec2 texCoord = getTextCoord();

    fs_color = blur13(texture_sampler, texCoord, screenSize, vec2(10, 0));
    fs_color += blur13(texture_sampler, texCoord, screenSize, vec2(-10, 0));

    fs_color /= 2;
}