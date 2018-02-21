/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.math

const val FUC_EPSILON: Float = 1.1920929E-7f

fun Float.isEpsilonZero(): Boolean {
    return -FUC_EPSILON < this && this < FUC_EPSILON
}

fun Float.isEpsilonEqual(b: Float): Boolean {
    return (this - b).isEpsilonZero()
}

fun Vector4.isEpsilonZeroLength(): Boolean {
    return length().isEpsilonZero()
}