#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require

precision highp float;
precision mediump sampler2D;
precision highp int;

// This uniform is declared but NEVER used in the vertex shader.
// Same declaration exists in fragment shader. On NVIDIA Tegra, linking fails
// with "struct type mismatch between shaders for uniform (named tex)".
uniform samplerExternalOES tex;

in vec2 a_position;
out vec2 v_texCoord;

void main() {
    gl_Position = vec4(a_position, 0.0, 1.0);
    v_texCoord = a_position * 0.5 + 0.5;
}
