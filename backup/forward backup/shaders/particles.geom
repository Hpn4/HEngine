#version 330

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

in mat4  vs_projectionMatrix[];
in float vs_scale[];

out vec2 gs_texCoord;

void main()
{
    vec3 pos = gl_in[0].gl_Position.xyz;
    mat4 projectionMatrix = vs_projectionMatrix[0];
    float scale = vs_scale[0];

    vec3 bl = pos + vec3(-1, -1, 0) * scale; // Bottom left
    gl_Position = projectionMatrix * vec4(bl, 1);
    gs_texCoord = vec2(0.0, 0.0);
    EmitVertex();

    vec3 br = pos + vec3(1, -1, 0) * scale; // Bottom right
    gl_Position = projectionMatrix * vec4(br, 1);
    gs_texCoord = vec2(1.0, 0.0);
    EmitVertex();

    vec3 tl = pos + vec3(-1, 1, 0) * scale; // Top left
    gl_Position = projectionMatrix * vec4(tl, 1);
    gs_texCoord = vec2(0.0, 1.0);
    EmitVertex();

    vec3 tr = pos + vec3(1, 1, 0) * scale; // Top right
    gl_Position = projectionMatrix * vec4(tr, 1);
    gs_texCoord = vec2(1.0, 1.0);
    EmitVertex();

    EndPrimitive();
}