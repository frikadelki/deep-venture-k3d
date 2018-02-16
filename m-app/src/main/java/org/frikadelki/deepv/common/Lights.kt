/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common

import org.frikadelki.deepv.pipeline.math.Vector4

class Lights {
    class Direct(val direction: Vector4, val color: Vector4)
    class Point(val origin: Vector4, val color: Vector4)

    private val directLightsMutable = mutableListOf<Direct>()
    val directLights: List<Direct>
        get() = directLightsMutable

    private val pointLightsMutable = mutableListOf<Point>()
    val pointLights: List<Point>
        get() = pointLightsMutable

    val ambient = Vector4()

    fun add(direct: Direct) {
        directLightsMutable.add(direct)
    }

    fun add(point: Point) {
        pointLightsMutable.add(point)
    }
}