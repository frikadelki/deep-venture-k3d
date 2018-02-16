/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.program.Program
import org.frikadelki.deepv.pipeline.program.ProgramSource
import org.frikadelki.deepv.pipeline.program.UniformHandle
import org.frikadelki.deepv.pipeline.program.VertexAttributeHandle
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Pd00Program(pipeline: Pipeline) {
    private val programSource = ProgramSource(
            """
                precision mediump float;

                uniform mat4 viewProjectionMatrix;
                uniform mat4 modelMatrix;

                attribute vec4 vPosition;

                void main() {
                    gl_Position = viewProjectionMatrix * modelMatrix * vPosition;
                }

            """.trimIndent(),
            """
                precision mediump float;

                uniform vec4 vColor;

                void main() {
                    gl_FragColor = vColor;
                }

            """.trimIndent())

    private val program: Program = pipeline.loadProgram(programSource)

    private val viewProjectionMatrix: UniformHandle = program.uniform("viewProjectionMatrix")

    private val modelMatrix: UniformHandle = program.uniform("modelMatrix")

    private val vertexPosition: VertexAttributeHandle = program.vertexAttribute("vPosition")

    private val vertexColor: UniformHandle = program.uniform("vColor")

    fun enable() {
        program.use()
    }

    fun setViewProjectionMatrix(matrix: Matrix4) {
        viewProjectionMatrix.setMatrix(matrix)
    }

    fun setModelMatrix(matrix: Matrix4) {
        modelMatrix.setMatrix(matrix)
    }

    fun setVertexColor(color: Vector4) {
        vertexColor.setVector(color)
    }

    fun setVertexPosition(buffer: FloatBuffer, components: Vector4Components) {
        vertexPosition.enable()
        vertexPosition.setData(buffer, components, VertexAttributeHandle.ComponentType.FLOAT)
    }

    fun drawTriangles(indexBuffer: ShortBuffer) {
        program.drawTriangles(indexBuffer)
    }

    fun disable() {
        vertexPosition.disable()
    }

    fun dispose() {
        program.dispose()
    }
}