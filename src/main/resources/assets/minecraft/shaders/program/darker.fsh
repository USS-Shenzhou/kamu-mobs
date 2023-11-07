#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

#define PI 3.1415926538

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    //vec3 darkerColor = pow(cos(0.5 * PI * (1 - original.rgb)), vec3(3));
    //vec3 channelWeights = vec3(0.2126, 0.7152, 0.0722);
    //float luminance = original.r * 0.2126 + original.g * 0.7152 + original.b * 0.0722;
    //float luminance = original.r * 0.299 + original.g * 0.587 + original.b * 0.114;
    //luminance = pow(cos(0.5 * PI * (1 - luminance)), 0.8);
    //luminance = pow(luminance, 0.8);
    //fragColor = vec4(original.rgb * luminance, 1);

    vec3 darkerColor = pow(cos(0.5 * PI * (1 - original.rgb)), vec3(2.6));
    fragColor = vec4(darkerColor, 1);

    //fragColor = vec4(original.rgb, 1.0);

}
