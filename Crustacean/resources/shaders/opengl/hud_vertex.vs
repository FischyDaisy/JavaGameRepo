#version 450

layout (location=0) in vec2 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec4 color;

out vec2 outTexCoord;
out vec4 outColor;

uniform mat4 projModelMatrix;

void main()
{
    gl_Position = projModelMatrix * vec4(position, 0.0, 1.0);
    outTexCoord = texCoord;
    outColor = color;
}