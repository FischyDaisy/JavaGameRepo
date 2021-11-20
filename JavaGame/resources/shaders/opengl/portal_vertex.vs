#version 450

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec4 outTexCoord;

void main(void) {
	vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
	gl_Position = projectionMatrix * mvPos;
	outTexCoord = gl_Position;
}
