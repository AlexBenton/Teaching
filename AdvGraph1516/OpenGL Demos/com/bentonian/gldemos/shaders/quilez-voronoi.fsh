#version 330

// Created by inigo quilez - iq/2013
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
//
// https://www.shadertoy.com/view/ldl3W8
// http://www.iquilezles.org/www/articles/voronoilines/voronoilines.htm

uniform float Time;
uniform vec3 eyePosition;
uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

#define ANIMATE

vec2 polar(vec3 pos) {
  return vec2(acos(pos.x / length(pos)), atan(pos.y / pos.z));
}

vec2 hash2( vec2 p )
{
    // procedural white noise   
    return fract(sin(vec2(dot(p,vec2(127.1,311.7)),dot(p,vec2(269.5,183.3))))*43758.5453);
}

vec3 voronoi( in vec2 x )
{
    vec2 n = floor(x);
    vec2 f = fract(x);

    //----------------------------------
    // first pass: regular voronoi
    //----------------------------------
    vec2 mg, mr;

    float md = 8.0;
    float t = Time * 0.0005;
    for( int j=-1; j<=1; j++ )
    for( int i=-1; i<=1; i++ )
    {
        vec2 g = vec2(float(i),float(j));
        vec2 o = hash2( n + g );
        #ifdef ANIMATE
        o = 0.5 + 0.5*sin( t + 6.2831*o );
        #endif  
        vec2 r = g + o - f;
        float d = dot(r,r);

        if( d<md )
        {
            md = d;
            mr = r;
            mg = g;
        }
    }

    //----------------------------------
    // second pass: distance to borders
    //----------------------------------
    md = 8.0;
    for( int j=-2; j<=2; j++ )
    for( int i=-2; i<=2; i++ )
    {
        vec2 g = mg + vec2(float(i),float(j));
        vec2 o = hash2( n + g );
        #ifdef ANIMATE
        o = 0.5 + 0.5*sin( t + 6.2831*o );
        #endif  
        vec2 r = g + o - f;

        if( dot(mr-r,mr-r)>0.00001 )
        md = min( md, dot( 0.5*(mr+r), normalize(r-mr) ) );
    }

    return vec3( md, mr );
}

void main( void )
{
    vec2 p = polar(position);

    vec3 c = voronoi( 4.0 * p );

    // isolines
    vec3 col = c.x*(0.5 + 0.5*sin(64.0*c.x))*vec3(1.0);
    // borders  
    col = mix( vec3(1.0,0.6,0.0), col, smoothstep( 0.04, 0.07, c.x ) );
    // feature points
    float dd = length( c.yz );
    col = mix( vec3(1.0,0.6,0.1), col, smoothstep( 0.0, 0.12, dd) );
    col += vec3(1.0,0.6,0.1)*(1.0-smoothstep( 0.0, 0.04, dd));

    // Lighting
    vec3 n = normalize(normal);
    vec3 l = normalize(lightPosition - position);
    vec3 e = normalize(position - eyePosition);
    vec3 r = reflect(l, n);

    float ambient = 0.4; // A bit of extra base brightness
    float diffuse = 0.5 * clamp(0, dot(n, l), 1);
    float specular = 0.3 * pow(clamp(0, dot(e, r), 1), 2);

    fragmentColor = vec4(col * (ambient + diffuse + specular), 1.0);
}
