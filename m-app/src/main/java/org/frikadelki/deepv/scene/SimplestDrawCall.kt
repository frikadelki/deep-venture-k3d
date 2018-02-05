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
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.camera.setLookAt
import org.frikadelki.deepv.pipeline.camera.setPerspective


class SimplestDrawCall(private val pipeline: Pipeline) {
    // in counterclockwise order
    private val triangleVertices = floatArrayOf(
            0.0f, 0.0f, 0.622008459f,
            0.0f, -0.5f, -0.311004243f,
            0.0f, 0.5f, -0.311004243f)
    private val vertexPositionBuffer = directFloatBufferFromArray(triangleVertices)

    private val triangleTrianglesIndices = shortArrayOf(0, 1, 2)
    private val drawIndexBuffer = directShortBufferFormArray(triangleTrianglesIndices)

    private val triangleColor = Vector4(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private val viewMatrix: Matrix4 = Matrix4()
    private val projectionMatrix: Matrix4 = Matrix4()
    private val viewProjectionMatrix: Matrix4 = Matrix4()

    private val program: SimplestProgram = SimplestProgram(pipeline)

    init {
        viewMatrix.setLookAt(
                Vector4(1.0f, 0.0f, 0.0f),
                Vector4(0.0f, 0.0f, 0.0f),
                Vector4(0.0f, 0.0f, 1.0f))
        projectionMatrix.setIdentity()
        updateVPMatrix()
    }

    fun onViewportChange(width: Int, height: Int) {
        projectionMatrix.setPerspective(
                Math.toRadians(120.0).toFloat(),
                width.toFloat()/height.toFloat(),
                0.000001f, 2.0f)
        updateVPMatrix()
    }

    private fun updateVPMatrix() {
        viewProjectionMatrix
                .set(projectionMatrix)
                .multiply(viewMatrix)
    }

    fun draw() {
        program.draw { viewProjectionMatrixHandle, vertexPositionHandle, vertexColorHandle ->
            viewProjectionMatrixHandle.setMatrix(viewProjectionMatrix)
            vertexPositionHandle.setData(vertexPositionBuffer, VertexAttributeHandle.ComponentsCount.THREE, VertexAttributeHandle.ComponentType.FLOAT)
            vertexColorHandle.setVector(triangleColor)

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3, GLES20.GL_UNSIGNED_SHORT, drawIndexBuffer)
        }
    }
}