#version 330

int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"

const float a = 1.0;
const float b = 3.0;
vec3 forces[2];


float sdPlane(vec3 p, vec4 n) {
    return dot(p,n.xyz) + n.w;
}

float getMetaball(vec3 p, vec3 v) {
    float r = length(p - v);
    if (r < b / 3.0) {
        return a * (1.0 - 3.0 * r * r / b * b);
    } else if (r < b) {
        return (3.0 * a / 2.0) * (1.0 - r / b) * (1.0 - r / b);
    } else {
        return 0.0;
    }
}

float sdImplicitSurface(vec3 p) {
    float mb = getMetaball(p, forces[0]) + getMetaball(p, forces[1]);
    float minDist = min(length(p - forces[0]), length(p - forces[1]));

    // 1.2679529 is the x-intercept of the metaball expression - 0.5
    if (minDist > b) {
        return max (minDist - b, b - 1.2679529);
    } else {
        return b - sqrt(6.0 * mb) - 1.2679529;
    }
}

float getSdf(vec3 p) {
    float f = sdImplicitSurface(p);
    return f;
}

float getSdfWithPlane(vec3 p) {
    return min(getSdf(p), sdPlane(p, vec4(0,1,0,1)));
}

float diffuse(vec3 point,vec3 normal) {
    return clamp(dot(normal, normalize(lightPos - point)), 0.0, 1.0);
}

float getShadow(vec3 pt) {
    vec3 lightDir = normalize(lightPos - pt);
    float kd = 1.0;
    int step = 0;
    float t = 0.1;

    for (int step = 0; step < renderDepth; step++) {
        float d = getSdf(pt + t * lightDir);
        if (d < 0.001) {
            kd = 0.0;
        } else {
            kd = min(kd, 16.0 * d / t);
        }
        t += d;
        if (t > length(lightPos - pt) || step >= renderDepth || kd < 0.001) {
            break;
        }
    }
    return kd;
}

vec3 getGradient(vec3 pt) {
    return vec3(
        getSdfWithPlane(vec3(pt.x + 0.0001, pt.y, pt.z)) - getSdfWithPlane(vec3(pt.x - 0.0001, pt.y, pt.z)),
        getSdfWithPlane(vec3(pt.x, pt.y + 0.0001, pt.z)) - getSdfWithPlane(vec3(pt.x, pt.y - 0.0001, pt.z)),
        getSdfWithPlane(vec3(pt.x, pt.y, pt.z + 0.0001)) - getSdfWithPlane(vec3(pt.x, pt.y, pt.z - 0.0001)));
}

float weightNearZero(float f, float r) {
    return max(r - abs(fract(f + r) - r), 0.0) / r;
}

vec3 getFloorColor(vec3 pt) {
    float d = getSdf(pt);
    float gridBlack = max(weightNearZero(pt.x, 0.025), weightNearZero(pt.z, 0.025));
    float sdfIsocline = weightNearZero(d, 0.05);
    float distanceTaper = smoothstep(1.0, 0.0, (d - 3.0) / 3.0);
    float weight = max(distanceTaper * sdfIsocline, gridBlack);
    vec3 color = mix(white, black, weight);
    return color;
}

vec3 illuminate(vec3 pt) {
    vec3 color = (abs(pt.y + 1.0) < 0.001) ? getFloorColor(pt) : white;
    vec3 gradient = getGradient(pt);
    float diff = diffuse(pt.xyz, normalize(gradient));
    return (0.25 + diff * 1)  * color;
}

vec3 raymarch(vec3 rayorig, vec3 raydir) {
    vec3 pos = rayorig;
    float d = getSdfWithPlane(pos);
    int work = 0;

    for (int step = 0; step < renderDepth; step++) {
        work++;
        pos = pos + raydir * d;
        d = getSdfWithPlane(pos);
        if (abs(d) < 0.001) {
            break;
        }
    }

    return (abs(d) < 0.001) ? illuminate(pos) : getBackground(raydir);
}

void main() {
    forces[0] = vec3(-2.0, 0, 0);
    forces[1] = vec3(2.0 * sin(iGlobalTime * PI / 5.0), 0.0, 0.0);
    fragColor = vec4(raymarch(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
