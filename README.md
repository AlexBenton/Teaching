Sample code for Advanced Graphics course, Cambridge University.
First added Feb 5 2014.


# Warnings

You can use this code freely for inspiration, examples of how to do it, examples of how not to do it... take your pick.  If you re-use large chunks of the code verbatim, please credit me.  Some portions of the math library come from Graphics Gems and other online sources.  The GIF image encoder is also not my work.

This code comes with no warranty, express or implied.  It could break your PC, leak spoilers of your favorite shows, or insult your cat.  There should be unit tests, but there aren't.  There should be more cleanup and deallocation code for the OpenGL shader stuff, but there isn't.  Caveat emptor.

The code in this repo uses a Maven project (`pom.xml`) to pull down the OS-specific binaries for JOGL, the Java OpenGL bindings.  (It pulls down the jars for Guava, too.)


# Installation

1. Fire up eclipse.
   a. Make sure you've got m2e, the Maven plugin, installed.  If it isn't, you can install it going to Help / Install New Software... / [Select Eclipse Project updates] / Filter for 'Maven'.
2. Click on File / Import...
3. Select the "Existing Maven Projects" importer
4. Browse to the root of your project; the importer should find the pom.xml file.
5. Import it.  All the rest of the directory structure should follow.


# What's here

This project contains multiple `main()` routines, so it contains multiple discrete Java apps (and a lot of shared framework code.  The code breaks down into:

Main class                  | Description
----------------------------|------------
`com.bentonian.hellosquare` | Demo application for loading a minimal JOGL application and rendering with it.
`com.bentonian.helloshader` | Demo application for loading a minimal shader and rendering with it.
`com.bentonian.framework`   | A fairly sprawling library of support routines, including a scene graph.
`com.bentonian.raytrace`    | A very limited, low-powered, ray tracer engine.  Still has some bugs.
`com.bentonian.jogldemos`   | A suite of demo Java classes that show off various traits of OpenGL rendering.

The following classes have `main()` routines and are demos I've either shown, or used to create images for lectures:
  
  * `HelloSquare`
  * `HelloShader{1, 2, 3}`
  * `BlobbyDemo`
  * `HierarchyDemo`
  * `MorphDemo`
  * `ShaderDemo`
  * `TextureDemo`

The class MovieMakerBackbone uses the ray tracer to generate scene images and movies used in lectures.  I've included it because it shows off some of the ray tracer's features, but it's not a very interesting demo in its own right.

## Key controls

All the demo classes that derive from `JoglDemo` share a common set of key controls:

Key        | Command
-----------|---------
Mouse      | Spin around the origin
Escape     | Quit
PageUp     | Zoom in
PageDown   | Zoom out
`1`        | Reset to looking along the Z axis
`2`        | Reset to looking along the Y axis
`P`        | Capture screenshot

`HierarchyDemo` also accepts the following keys:

Key        | Command
-----------|---------
`+`        | Add a level
`-`        | Remove a level

`ShaderDemo` also accepts the following keys:

Key        | Command
-----------|---------
`+`        | Go to next shader
`-`        | Go to previous shader
`[`        | Go to previous model
`]`        | Go to next model
