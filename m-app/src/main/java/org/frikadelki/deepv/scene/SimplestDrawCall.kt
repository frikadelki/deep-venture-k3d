/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/4
 */

package org.frikadelki.deepv.scene

import android.opengl.GLES20
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.VertexAttributeHandle
import org.frikadelki.deepv.pipeline.directFloatBufferFromArray
import org.frikadelki.deepv.pipeline.directShortBufferFormArray


class SimplestDrawCall(private val pipeline: Pipeline) {
    // in counterclockwise order
    private val triangleVertices = floatArrayOf(
            0.0f, 0.622008459f, 0.0f,   // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f   // bottom right
    )
    private val triangleTrianglesIndices = shortArrayOf(0, 1, 2)

    private val triangleColor = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private val vertexPositionBuffer = directFloatBufferFromArray(triangleVertices)
    private val drawIndexBuffer = directShortBufferFormArray(triangleTrianglesIndices)

    private val program: SimplestProgram = SimplestProgram(pipeline)

    fun draw() {
        program.draw { vertexPosition, vertexColor ->
            vertexPosition.setData(vertexPositionBuffer, VertexAttributeHandle.ComponentsCount.THREE, VertexAttributeHandle.ComponentType.FLOAT)
            vertexColor.setVector(triangleColor)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3, GLES20.GL_UNSIGNED_SHORT, drawIndexBuffer)
        }
    }
}