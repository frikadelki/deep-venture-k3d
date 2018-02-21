/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.common

import org.frikadelki.deepv.pipeline.camera.setLookAt
import org.frikadelki.deepv.pipeline.camera.setPerspective
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4RO
import org.frikadelki.kash.klang.dirtyProperty

class Camera {
    private val fovYDegrees: Float = 90.0f
    private val zNear: Float = 0.1f
    private val zFar: Float = 50.0f

    private val viewM: Matrix4 = Matrix4().setE()
    private val projectionM: Matrix4 = Matrix4().setE()

    val eyePosition: Vector4 = Vector4()

    private val computedViewProjectionM: Matrix4 = Matrix4()
    private val viewProjectionMProperty = dirtyProperty {
        computedViewProjectionM
                .set(projectionM)
                .multiply(viewM)
    }

    val viewProjectionMatrix by viewProjectionMProperty

    fun setLookAt(eyePosition: Vector4RO,
                  lookAtCenter: Vector4RO,
                  cameraUp: Vector4RO) {
        this.eyePosition.set(eyePosition)
        viewM.setLookAt(eyePosition, lookAtCenter, cameraUp)
        viewProjectionMProperty.markDirty()
    }

    fun setViewport(width: Int, height: Int) {
        val aspectRatioWH = width.toFloat() / height.toFloat()
        projectionM.setPerspective(fovYDegrees, aspectRatioWH, zNear, zFar)
        viewProjectionMProperty.markDirty()
    }
}