# NVIDIA Tegra `samplerExternalOES` Shader Linker Bug

Minimal reproduction of a driver bug on NVIDIA Tegra devices where `glLinkProgram` incorrectly fails when both vertex and fragment shaders declare the same `uniform samplerExternalOES`, even when the vertex shader does not use it.

## The Bug

When linking a program where **both** shaders declare:

```glsl
uniform samplerExternalOES tex;
```

and the **vertex shader declares but never uses** the uniform, the Tegra driver incorrectly reports a link error:

```
glLinkProgram FAILED:
Vertex info
-----------
error: struct type mismatch between shaders for uniform (named tex)
```

### Expected behavior (per GLSL spec)

An unused uniform is valid. The program link should **succeed**. Other GLES implementations handle this correctly.

### Actual behavior (NVIDIA Tegra)

The link **fails** with a bogus "struct type mismatch" error, despite both shaders declaring the same type (`samplerExternalOES`).

## Shaders

- **Vertex** (`res/raw/vertex.glsl`): Declares `uniform samplerExternalOES tex` but does not use it.
- **Fragment** (`res/raw/fragment.glsl`): Declares and uses `uniform samplerExternalOES tex`.

Both declare the same uniform with the same type. The vertex shader’s declaration is intentionally unused to trigger the Tegra bug.

PS: This bug is currently been tracked here: https://forums.developer.nvidia.com/t/nvidia-tegra-samplerexternaloes-shader-linker-bug/364470
