#version 330

uniform sampler2D texture_Diffuse;

in vec4 pass_Color;
in vec2 pass_TextureCoord;

out vec4 out_Color;

void main() {
	// the texture function is used to sample surrounding texels in order to determine the desired color
	// out_Color = texture(texture_Diffuse, pass_TextureCoord); // pure texture
	out_Color = pass_Color * texture(texture_Diffuse, pass_TextureCoord); // texture with colorful overlay
}