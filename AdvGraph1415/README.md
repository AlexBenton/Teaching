Sample code for Advanced Graphics course, Cambridge University.


# Warning

This code comes with no warranty, express or implied.  It could break your PC, leak spoilers of your favorite shows, or insult your cat.  There should be unit tests, but there aren't.  There should be more cleanup and deallocation code for the OpenGL shader stuff, but there isn't.  Caveat emptor.

You can use this code freely for inspiration, examples of how to do it, examples of how not to do it... take your pick.  If you re-use large chunks of the code verbatim, please credit me.  Some portions of the math library come from Graphics Gems and other online sources.  The GIF image encoder is also not my work.


# Installation

These demos are in two parts: a `framework` project, which contains all the libraries used by all the sample code, and an `OpenGL Demos` project, which contains all the actual demos.

#### Eclipse setup
1. Make sure you've got the '[m2e](http://download.eclipse.org/technology/m2e/releases/)' eclipse plugin installed.  This is Maven, which will download supporting libraries for you.
2. Make sure you've got the '[mavennatives](https://code.google.com/p/mavennatives/)' eclipse plugin installed.  This is a support module for Maven which can download OS-specific binary libraries.

#### Framework
1. Click on File / Import...
2. Select the "Existing Maven Projects" importer
3. Browse to the root of the `framework` project; the importer should find the pom.xml file.
4. Import it.  All the rest of the directory structure should follow.
5. Right-click on the `framework` project in Eclipse, choose Properties.
  1. Go to Java Build Path
  2. Choose 'Order and Export'
  3. Check the 'Maven Dependencies' export
  4. Hit OK

#### OpenGL Demos
1. Click on File / Import...
2. Select the "Existing Projects into Workspace" importer
3. Browse to the root of the `OpenGL Demos` project.
4. Import the project.  It should automatically pick up a project dependency on `framework`.

# What's in these demos

The `OpenGL Demos` project contains multiple `main()` routines, so it contains multiple discrete Java apps.  These are:

Main class                                | Description
------------------------------------------|------------
`com.bentonian.gldemos.bezier`            | Bivariate 4x4 Bezier patch
`com.bentonian.gldemos.blobby`            | Implicit surface ("Blobby") model animation
`com.bentonian.gldemos.hierarchy`         | Recursive scene graph generates fractal geometry
`com.bentonian.gldemos.morph`             | Interpolated animation of four parametric surfaces
`com.bentonian.gldemos.offscreenrender`   | Simple scene showing the use of off-screen rendering
`com.bentonian.gldemos.raytracedtexture`  | Simple raytracer demo on textured primitives
`com.bentonian.gldemos.shaders`           | A suite of GLSL shaders applied to a suite of different models
`com.bentonian.gldemos.subdivision`       | Loop, Doo-Sabin and Catmull-Clark subdivision

## Key controls

All the demo classes share a common set of mouse and key controls:

Key        | Command
-----------|---------
Mouse      | Spin around the origin
`Escape`   | Quit
`PageUp`   | Zoom in
`PageDown` | Zoom out
`1`        | Reset to looking along the Z axis
`2`        | Reset to looking along the Y axis
`P`        | Capture screenshot

`HierarchyDemo` also accepts the following keys:

Key        | Command
-----------|---------
`+`        | Add a level
`-`        | Remove a level

`RayTracedTextureDemo` also accepts the following keys:

Key        | Command
-----------|---------
`r`        | Ray trace current view

`ShaderDemo` also accepts the following keys:

Key        | Command
-----------|---------
`+`        | Go to next shader
`-`        | Go to previous shader
`[`        | Go to previous model
`]`        | Go to next model

`SubdivisionDemo` also accepts the following keys:

Key        | Command
-----------|---------
`+`        | Refine one level with current scheme
`-`        | Go up one level of refinement
`[`        | Go to previous subdivision scheme
`]`        | Go to next subdivision scheme
`k`        | Go to previous model
`j`        | Go to next model
`e`        | Toggle surface edges
`n`        | Toggle surface normals