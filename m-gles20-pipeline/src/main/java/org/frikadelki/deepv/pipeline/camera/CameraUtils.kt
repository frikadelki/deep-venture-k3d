/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.camera

import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4RO
import android.opengl.Matrix as MatrixUtils

fun Matrix4.setFrustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
    MatrixUtils.frustumM(rawData, rawOffset, left, right, bottom, top, near, far)
    return this
}

fun Matrix4.setPerspective(fovYDegrees: Float, aspectRatioWH: Float, zNear: Float, zFar: Float): Matrix4 {
    val fovYRad = Math.toRadians(fovYDegrees.toDouble())
    val top: Float = (Math.tan(fovYRad/2.0) * zNear).toFloat()
    val bottom = -1.0f * top
    val left = aspectRatioWH * bottom
    val right = aspectRatioWH * top
    return setFrustum(left, right, bottom, top, zNear, zFar)
}

fun Matrix4.setOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
    MatrixUtils.orthoM(rawData, rawOffset, left, right, bottom, top, near, far)
    return this
}

fun Matrix4.setLookAt(eye: Vector4RO, center: Vector4RO, up: Vector4RO): Matrix4 {
    MatrixUtils.setLookAtM(rawData, rawOffset,
            eye.x, eye.y, eye.z,
            center.x, center.y, center.z,
            up.x, up.y, up.z)
    return this
}