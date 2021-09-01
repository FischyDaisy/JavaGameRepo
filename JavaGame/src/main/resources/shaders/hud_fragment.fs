#version 450

in vec2 outTexCoord;
in vec4 outColor;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform int hasTexture;

void main()
{
    if ( hasTexture == 1 )
    {
        fragColor = outColor * texture(texture_sampler, outTexCoord);
    }
    else
    {
        fragColor = outColor;
    }
}