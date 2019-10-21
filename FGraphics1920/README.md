Sample code for Further Graphics course, Cambridge University.


# Warning

This code comes with no warranty, express or implied.  It could break your PC, leak spoilers of your favorite shows, or insult your cat.  There should be unit tests, but there aren't.  There should be more cleanup and deallocation code for the OpenGL shader stuff, but there isn't.  Caveat emptor.

You can use this code freely for inspiration, examples of how to do it, examples of how not to do it... take your pick.  If you re-use large chunks of the code verbatim, please credit me.  Some portions of the math library were inspired by Graphics Gems and other online sources.  The GIF image encoder is also not my work.


# Installation

These demos are in two parts: a `framework` project, which contains all the libraries used by all the sample code, and an `OpenGL Demos` project, which contains all the actual demos.

#### Eclipse setup
1. Install [Eclipse](https://eclipse.org/downloads/).  You can also use IntelliJ or other Java compilers, but we've had reports of difficulty accessing compiled resources from IntelliJ.
2. Make sure you've got the '[m2e](http://download.eclipse.org/technology/m2e/releases/)' (Maven for Eclipse) eclipse plugin installed.  This is Maven, which will download supporting libraries for you.  It's installed by default in most Eclipse installs.

The `framework` project includes a `pom.xml` file which should automatically download and install LWJGL.  The version of LWJGL downloaded is specified in the `<lwjgl.version>` value.  If LWJGL does not download automatically (we've only tested in limited configurations) you can also download it yourself from [www.lwjgl.org](https://www.lwjgl.org/download).

#### Framework
1. Click on File / Import...
2. Select the "Existing Maven Projects" importer
3. Browse to the root of the `framework` project; the importer should find the pom.xml file.
4. Import it.  All the rest of the directory structure should follow.
5. Right-click on the `framework` project in Eclipse, choose Properties.
  1. Go to Java Build Path
  2. Choose 'Order and Export'
  3. Tick the 'Maven Dependencies' export
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
`com.bentonian.gldemos.beziercurve`       | Monovariate Bezier spline
`com.bentonian.gldemos.bezierpatch`       | Bivariate 4x4 Bezier patch
`com.bentonian.gldemos.blobby`            | Implicit surface ("Blobby") model animation
`com.bentonian.gldemos.marchingcubes`     | Marching cubes in action
`com.bentonian.gldemos.particles`         | Simple particle system
`com.bentonian.gldemos.sdfpolygonization` | Generating polygons from a GPU-only SDF described in GLSL
`com.bentonian.gldemos.sdfrender`         | Examples of sphere-marched signed distance field geometry
`com.bentonian.gldemos.shaders`           | A suite of GLSL shaders applied to a suite of polygonal models
`com.bentonian.gldemos.subdivision`       | Loop, Doo-Sabin and Catmull-Clark subdivision
`com.bentonian.gldemos.voronoi`           | Interactive Voronoi Diagram

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
`3`        | Reset to looking along the X axis
`Ctrl+P`   | Capture screenshot

`BlobbyDemo` also accepts the following keys:

Key        | Command
-----------|---------
`space`   | Pause / Unpause
`b`        | Toggle octree boxes
`c`        | Toggle color shading per blobby
`e`        | Toggle surface edges
`f`        | Toggle surface faces
`l`        | Toggle blobby function subframe
`s`        | Toggle smoothed implicit surface edge interpolation
`-`        | Lower target refinement level
`=`        | Raise target refinement level
(drag)     | Drag control point with mouse

`SDFPolygonizationDemo` also accepts the following keys:

Key        | Command
-----------|---------
`b`        | Toggle octree boxes
`e`        | Toggle surface edges
`f`        | Toggle surface faces

`SDFRenderDemo` also accepts the following keys:

Key        | Command
-----------|---------
`4`        | Move camera to a view of all three axes
`Space`   | Pause animations
`[`        | Go to previous scene
`]`        | Go to next scene
`Ctrl+R`   | Record animated gif

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

`VoronoiDemo` also accepts the following keys:

Key        | Command
-----------|---------
`[`        | Go to previous model
`]`        | Go to next model
(click)    | Add up to 16 points in the Voronoi shader
