/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.scene

import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.kash.klang.dirtyProperty
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class SimplestPrimitive(val vertexPositionsBuffer: FloatBuffer,
                             val vertexPositionComponents: Vector4Components,
                             val geometryIndexBuffer: ShortBuffer) {

    private val centerScaleMatrix: Matrix4 = Matrix4().setE()
    private val centerRotationMatrix: Matrix4 = Matrix4().setE()
    private val worldPositionMatrix: Matrix4 = Matrix4().setE()

    private val computedModelMatrix = Matrix4().setE()
    private val modelMatrixProperty = dirtyProperty {
        computedModelMatrix.setE()
                .multiply(worldPositionMatrix)
                .multiply(centerRotationMatrix)
                .multiply(centerScaleMatrix)
    }

    val modelMatrix by modelMatrixProperty

    val colorLuminosity: Vector4 = Vector4().point()

    fun centerScale(scale: Vector4) {
        centerScaleMatrix.scale(scale)
        modelMatrixProperty.markDirty()
    }

    fun centerRotate(axis: Vector4, angleDegrees: Float) {
        centerRotationMatrix.rotate(axis, angleDegrees)
        modelMatrixProperty.markDirty()
    }

    fun worldPositionTranslate(d: Vector4) {
        worldPositionMatrix.translate(d)
        modelMatrixProperty.markDirty()
    }
}