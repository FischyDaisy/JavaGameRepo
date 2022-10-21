#version 450

in vec4 outTexCoord;

uniform sampler2D texture_sampler;

//Outputs
out vec4 FragColor;

void main(void) {
	vec2 uv = (outTexCoord.xy / outTexCoord.w);
	uv = uv*0.5 + 0.5;
	FragColor = vec4(texture2D(texture_sampler, uv).rgb, 1.0);
}