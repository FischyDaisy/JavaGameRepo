#version 450

uniform sampler2D texture_sampler;

out vec4 fragColor;

void main(void) {
	fragColor = vec4(1.0, 0.0, 1.0, 1.0);
}