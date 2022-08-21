#version 450

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec3 tangent;
layout(location = 3) in vec3 bitangent;
layout(location = 4) in vec2 textCoords;

// Instanced attributes
layout (location = 5) in mat4 modelMatrix;
layout (location = 9) in uint matIdx;

void main()
{
    gl_Position = modelMatrix * vec4(position, 1.0f);
}
