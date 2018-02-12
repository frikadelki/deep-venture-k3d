/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/4
 */

package org.frikadelki.deepv.scene

import android.opengl.GLES20
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.VertexAttributeHandle
import org.frikadelki.deepv.pipeline.camera.setLookAt
import org.frikadelki.deepv.pipeline.camera.setPerspective
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.v4AxisZ
import org.frikadelki.deepv.pipeline.math.v4Point
import org.frikadelki.deepv.pipeline.math.v4Vector


class SimplestDrawCall(private val pipeline: Pipeline) {
    private val viewMatrix: Matrix4 = Matrix4()
    private val projectionMatrix: Matrix4 = Matrix4()
    private val viewProjectionMatrix: Matrix4 = Matrix4()

    init {
        val eyePosition = v4Point(1.5f, 0.0f, 0.5f)
        val lookAtCenter = v4Point()
        val cameraUp = v4AxisZ()
        viewMatrix.setLookAt(eyePosition, lookAtCenter, cameraUp)
        projectionMatrix.setE()
        updateVPMatrix()
    }

    private val program: SimplestProgram = SimplestProgram(pipeline)

    private val scubeScaleFactor = 1.1f
    private val scubeRotationSpeed = 0.05f
    private val scubePrimitive = primitiveCube()
    init {
        scubePrimitive.centerScale(Vector4(scubeScaleFactor, scubeScaleFactor, scubeScaleFactor))
        scubePrimitive.worldPositionTranslate(Vector4(y = 0.7f))
        scubePrimitive.colorLuminosity.set(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
    }

    private val bubePrimitive = primitiveCube()
    init {
        bubePrimitive.centerRotate(v4AxisZ(), 45.0f)
        bubePrimitive.worldPositionTranslate(v4Vector(x = -0.5f, z = 0.2f))
        bubePrimitive.colorLuminosity.set(0.0f, 0.76953125f, 0.52265625f, 1.0f)
    }

    fun onUpdateAnimations(deltaMillis: Long) {
        val rotationAngle = deltaMillis * scubeRotationSpeed
        scubePrimitive.centerRotate(v4AxisZ(), rotationAngle)
    }

    fun onDraw() {
        drawPrimitive(scubePrimitive)
        drawPrimitive(bubePrimitive)
    }

    private fun drawPrimitive(primitive: SimplestPrimitive) {
        program.draw({ viewProjectionMatrixHandle,
                       modelMatrixHandle,
                       vertexPositionHandle,
                       vertexColorHandle ->
            // uniforms
            viewProjectionMatrixHandle.setMatrix(viewProjectionMatrix)
            modelMatrixHandle.setMatrix(primitive.modelMatrix)
            vertexColorHandle.setVector(primitive.colorLuminosity)

            // vertex position data
            val vertexBuffer = primitive.vertexPositionsBuffer
            vertexBuffer.position(0)
            vertexPositionHandle.setData(
                    vertexBuffer,
                    primitive.vertexPositionComponents,
                    VertexAttributeHandle.ComponentType.FLOAT)

            // draw call
            val indexBuffer = primitive.geometryIndexBuffer
            indexBuffer.position(0)
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    indexBuffer.capacity(),
                    GLES20.GL_UNSIGNED_SHORT,
                    indexBuffer)
        })
    }

    fun onViewportChange(width: Int, height: Int) {
        projectionMatrix.setPerspective(
                90.0f,
                width.toFloat()/height.toFloat(),
                0.1f, 50.0f)
        updateVPMatrix()
    }

    private fun updateVPMatrix() {
        viewProjectionMatrix
                .set(projectionMatrix)
                .multiply(viewMatrix)
    }
}