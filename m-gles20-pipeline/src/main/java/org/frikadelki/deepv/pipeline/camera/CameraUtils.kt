/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.camera

import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import android.opengl.Matrix as MatrixUtils

fun Matrix4.setFrustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
    rawAccess { data, offset ->
        MatrixUtils.frustumM(data, offset, left, right, bottom, top, near, far)
    }
    return this
}

fun Matrix4.setPerspective(fovYRad: Float, aspectRatioWH: Float, zNear: Float, zFar: Float): Matrix4 {
    val top: Float = (Math.tan(fovYRad/2.0) * zNear).toFloat()
    val bottom = -1.0f * top
    val left = aspectRatioWH * bottom
    val right = aspectRatioWH * top
    return setFrustum(left, right, bottom, top, zNear, zFar)
}

fun Matrix4.setLookAt(eye: Vector4, center: Vector4, up: Vector4): Matrix4 {
    rawAccess { data, offset ->
        MatrixUtils.setLookAtM(data, offset,
                eye.x, eye.y, eye.z,
                center.x, center.y, center.z,
                up.x, up.y, up.z)
    }
    return this
}