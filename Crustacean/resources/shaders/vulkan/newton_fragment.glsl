#version 450

layout (push_constant) uniform PushConstants {
    uint selected;
} pushConstants;

layout (location = 0) out vec4 outColor;

void main() {
    if (selected > 0) {
        outColor = vec4(1.f, 0.f, 0.f, 1.f);
    } else {
        outColor = vec4(0.f, 1.f, 0.f, 1.f);
    }
}