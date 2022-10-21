#version 450

in vec2 outTexCoord;
in vec4 outColor;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec4 color;
uniform int hasTexture;
uniform int inGame;

void main()
{
    if (inGame == 1) 
    {
    	if ( hasTexture == 1 )
    	{
        	fragColor = color * texture(texture_sampler, outTexCoord);
    	}
    	else
    	{
        	fragColor = color;
    	}
    }
    else 
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
}