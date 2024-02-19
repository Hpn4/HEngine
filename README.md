# HEngine

A simple and terribly inefficient game engine written in Java (using LWJGL) during my school years. It was a fun project to work on and I learned a lot from it. It's not really useful for anything, but I'm keeping it here for sentimental reasons.

## Features

I implemented a lot of features in this engine, but most of them are not very efficient.
I was still learning about 3D rendering at the time, so I didn't really care about performance.
I was more interested in learning how to implement these features than making them efficient.
Here are some of the features I implemented:

Lights:
- Directional lights
- Point lights
- Spot lights

Meshes:
- Support animated models
- Support most common file formats (all supported by Assimp)
- Basic support of transparency

VFX and advanced rendering techniques:
- Particle systems
- Fog
- Skybox
- SSAO
- Bloom
- Shadows
- Decals

Optimization:
- Frustum culling
- Mesh Instancing
- GBuffers (deferred rendering)

## Screenshots

![](resources\screenshot\1.png)

## Technical details

I used LWJGL with the following libraries:
- Assimp for model loading
- STB for image loading
- JOML for math
- NanoVG for UI rendering
- OpenGL for rendering
- GLFW for window management
- OpenAL for audio

I also create a small lib on top of GLFW and NanoVG to create a swing like UI system.