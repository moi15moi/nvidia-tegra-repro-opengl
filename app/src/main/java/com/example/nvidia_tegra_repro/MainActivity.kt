package com.example.nvidia_tegra_repro

import android.app.Activity
import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Minimal reproduction of NVIDIA Tegra samplerExternalOES shader linker bug.
 *
 * Compiles and links vertex+fragment shaders where BOTH declare
 * uniform samplerExternalOES tex. Vertex shader does not use it.
 *
 * Expected: Link succeeds (unused uniform is valid)
 * Actual on Tegra: "struct type mismatch between shaders for uniform (named tex)"
 */
class ReproActivity : Activity() {

    companion object {
        private const val TAG = "TegraRepro"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(3)
            setEGLConfigChooser(8, 8, 8, 8, 0, 0)
            setRenderer(ReproRenderer(this@ReproActivity))
        }
        setContentView(glView)
    }

    private class ReproRenderer(private val context: Context) : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            val vertexSrc = loadShader(R.raw.vertex)
            val fragmentSrc = loadShader(R.raw.fragment)

            val vsh = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER)
            GLES32.glShaderSource(vsh, vertexSrc)
            GLES32.glCompileShader(vsh)
            checkCompileStatus(vsh, "vertex")

            val fsh = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER)
            GLES32.glShaderSource(fsh, fragmentSrc)
            GLES32.glCompileShader(fsh)
            checkCompileStatus(fsh, "fragment")

            val program = GLES32.glCreateProgram()
            GLES32.glAttachShader(program, vsh)
            GLES32.glAttachShader(program, fsh)
            GLES32.glLinkProgram(program)

            val linkStatus = IntArray(1)
            GLES32.glGetProgramiv(program, GLES32.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == GLES32.GL_FALSE) {
                val log = GLES32.glGetProgramInfoLog(program)
                Log.e(TAG, "glLinkProgram FAILED:")
                Log.e(TAG, log ?: "(no log)")
                Log.e(TAG, "This is the Tegra samplerExternalOES bug.")
            } else {
                Log.i(TAG, "glLinkProgram succeeded (unexpected on affected Tegra devices)")
            }

            GLES32.glDeleteShader(vsh)
            GLES32.glDeleteShader(fsh)
            GLES32.glDeleteProgram(program)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
        override fun onDrawFrame(gl: GL10?) {}

        private fun loadShader(resId: Int): String {
            context.resources.openRawResource(resId).use { ins ->
                return ins.bufferedReader().readText()
            }
        }

        private fun checkCompileStatus(shader: Int, name: String) {
            val status = IntArray(1)
            GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, status, 0)
            if (status[0] == GLES32.GL_FALSE) {
                val log = GLES32.glGetShaderInfoLog(shader)
                Log.e(TAG, "$name shader compile failed: $log")
            }
        }
    }
}