#version 450

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 textCoords;
layout (location = 2) in vec4 color;

layout (push_constant) uniform PushConstants {
    mat4 ortho;
} pushConstants;

layout (location = 0) out vec2 outTextCoords;
layout (location = 1) out vec4 outColor;

out gl_PerVertex
{
    vec4 gl_Position;
};

void main()
{
    outTextCoords = textCoords;
    outColor = color;
    gl_Position = pushConstants.ortho * vec4(position, 0.0, 1.0);
}