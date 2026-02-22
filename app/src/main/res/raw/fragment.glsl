#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require

precision highp float;
precision mediump sampler2D;
precision highp int;

uniform samplerExternalOES tex;

in vec2 v_texCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(tex, v_texCoord);
}
