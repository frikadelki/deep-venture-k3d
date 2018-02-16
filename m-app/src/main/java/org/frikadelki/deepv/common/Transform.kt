/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/12
 */

package org.frikadelki.deepv.common

import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.kash.klang.dirtyProperty

class Transform {
    private val selfScaleMatrix: Matrix4 = Matrix4().setE()
    private val selfRotationMatrix: Matrix4 = Matrix4().setE()
    private val worldPositionMatrix: Matrix4 = Matrix4().setE()

    private val computedModelMatrix = Matrix4().setE()
    private val modelMatrixProperty = dirtyProperty {
        computedModelMatrix.setE()
                .multiply(worldPositionMatrix)
                .multiply(selfRotationMatrix)
                .multiply(selfScaleMatrix)
    }

    val modelMatrix by modelMatrixProperty

    fun selfScale(scale: Vector4) {
        selfScaleMatrix.scale(scale)
        modelMatrixProperty.markDirty()
    }

    fun selfRotate(axis: Vector4, angleDegrees: Float) {
        selfRotationMatrix.rotate(axis, angleDegrees)
        modelMatrixProperty.markDirty()
    }

    fun worldTranslate(d: Vector4) {
        worldPositionMatrix.translate(d)
        modelMatrixProperty.markDirty()
    }
}