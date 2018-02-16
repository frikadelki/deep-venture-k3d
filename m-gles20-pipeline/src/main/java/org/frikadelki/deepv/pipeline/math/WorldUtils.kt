/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/11
 */

package org.frikadelki.deepv.pipeline.math

object World {
    const val C0: Float = 0.0f
    const val C1: Float = 1.0f

    const val ARC_000: Float = 0.0f
    const val ARC_045: Float = 45.0f
    const val ARC_090: Float = 90.0f
    const val ARC_180: Float = 180.0f
    const val ARC_270: Float = 270.0f
    const val ARC_360: Float = 360.0f
}

fun v4Point(x: Float = World.C0,
            y: Float = World.C0,
            z: Float = World.C0,
            out: Vector4 = Vector4()) = out.point(x, y, z)

fun v4Vector(x: Float = World.C0,
             y: Float = World.C0,
             z: Float = World.C0,
             w: Float = World.C0,
             out: Vector4 = Vector4()) = out.set(x, y, z, w)

fun v4Color(r: Float = World.C0,
            g: Float = World.C0,
            b: Float = World.C0,
            a: Float = World.C1,
            out: Vector4 = Vector4()) = out.set(r, g, b, a)

fun v4AxisX(out: Vector4 = Vector4()) = v4Vector(x = World.C1, out = out)

fun v4AxisY(out: Vector4 = Vector4()) = v4Vector(y = World.C1, out = out)

fun v4AxisZ(out: Vector4 = Vector4()) = v4Vector(z = World.C1, out = out)