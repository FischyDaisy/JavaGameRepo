#version 450

layout(location = 0) in vec3 position;
layout (location = 1) in mat4 modelMatrix;

out gl_PerVertex {
    vec4 gl_Position;
};

layout(set = 0, binding = 0) uniform ProjUniform {
    mat4 projectionMatrix;
} projUniform;
layout(set = 1, binding = 0) uniform ViewUniform {
    mat4 viewMatrix;
} viewUniform;

void main() {
    mat4 modelViewMatrix = viewUniform.viewMatrix * modelMatrix;
    gl_Position = projUniform.projectionMatrix * modelViewMatrix * vec4(position, 1);
}