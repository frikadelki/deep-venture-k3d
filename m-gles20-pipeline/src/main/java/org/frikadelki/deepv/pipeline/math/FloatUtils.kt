/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.math

const val FUC_EPSILON: Float = 1.1920929E-7f

fun isEpsilonZero(value: Float): Boolean {
    return -FUC_EPSILON < value && value < FUC_EPSILON
}

fun isEpsilonEqual(a: Float, b: Float): Boolean {
    return isEpsilonZero(a - b)
}