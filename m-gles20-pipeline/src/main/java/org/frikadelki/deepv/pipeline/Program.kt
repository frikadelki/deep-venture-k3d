/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import android.opengl.GLES20


class ProgramException : Throwable {
    constructor() : super()
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}

data class ProgramSource(
        val vertexShader: String,
        val fragmentShader: String)

class Program internal constructor(private val program: Int, source: ProgramSource) {
    private val vertexShader: Int
    private val fragmentShader: Int

    private var disposed: Boolean = false

    init {
        try {
            vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, source.vertexShader)
        } catch (e: ProgramException) {
            GLES20.glDeleteProgram(program)
            throw e
        }

        try {
            fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, source.fragmentShader)
        } catch (e: ProgramException) {
            GLES20.glDeleteProgram(program)
            GLES20.glDeleteShader(vertexShader)
            throw e
        }

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        if (glErred()) {
            dispose()
            throw ProgramException("Failed to attach shaders and link program.")
        }
    }

    fun use() {
        checkDisposed()
        GLES20.glUseProgram(program)
        if (glErred()) {
            throw ProgramException("Failed to use program.")
        }
    }

    fun vertexAttribute(name: String): VertexAttributeHandle {
        checkDisposed()
        val handle = GLES20.glGetAttribLocation(program, name)
        if (-1 == handle) {
            glClearErrors()
            throw ProgramException("Failed to locate vertex attribute '$name'.")
        }
        return VertexAttributeHandle(handle, ::checkDisposed)
    }

    fun uniform(name: String): UniformHandle {
        checkDisposed()
        val handle = GLES20.glGetUniformLocation(program, name)
        if (-1 == handle) {
            glClearErrors()
            throw ProgramException("Failed to locate uniform '$name'.")
        }
        return UniformHandle(handle)
    }

    private fun checkDisposed() {
        if (disposed) {
            throw ProgramException("Attempt to use disposed program.")
        }
    }

    fun dispose() {
        if (disposed) {
            return
        }
        disposed = true
        GLES20.glDetachShader(program, vertexShader)
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDetachShader(program, fragmentShader)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteProgram(program)
        glClearErrors()
    }

    private fun loadShader(type: Int, shaderSource: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (0 == shader) {
            glClearErrors()
            throw ProgramException("Failed to create shader.")
        }

        GLES20.glShaderSource(shader, shaderSource)
        if (glErred()) {
            throw ProgramException("Failed to attach shader source.")
        }

        val compiled = intArrayOf(GLES20.GL_FALSE)
        GLES20.glCompileShader(shader)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (GLES20.GL_TRUE != compiled[0]) {
            glClearErrors()
            throw ProgramException(GLES20.glGetShaderInfoLog(shader))
        }

        return shader
    }
}