#version 330

in vec4 in_Position;
in vec4 in_Color;

out vec4 pass_Color;

void main() {
	gl_Position = in_Position;
	pass_Color = in_Color;
}