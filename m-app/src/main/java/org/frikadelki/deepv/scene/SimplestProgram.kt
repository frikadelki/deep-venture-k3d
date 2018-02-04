/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.scene

import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.Program
import org.frikadelki.deepv.pipeline.UniformHandle
import org.frikadelki.deepv.pipeline.VertexAttributeHandle
import org.frikadelki.deepv.pipeline.glErred


class SimplestProgram(private val pipeline: Pipeline) {
    private val programSource = org.frikadelki.deepv.pipeline.ProgramSource(
            """
                precision mediump float;

                attribute vec4 vPosition;

                void main() {
                    gl_Position = vPosition;
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

    private val vertexPosition: VertexAttributeHandle = program.vertexAttribute("vPosition")

    private val vertexColor: UniformHandle = program.uniform("vColor")

    fun draw(drawCall: (vertexPosition: VertexAttributeHandle, vertexColor: UniformHandle) -> Unit) {
        program.use()
        vertexPosition.enable()
        drawCall(vertexPosition, vertexColor)
        if (glErred()) {
            TODO()
        }
        vertexPosition.disable()
    }
}