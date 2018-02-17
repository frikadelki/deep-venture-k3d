/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import org.frikadelki.deepv.common.mesh.AbcMeshRaw
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesRaw
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.World
import org.frikadelki.deepv.pipeline.math.v4Point


class AbcMorphingSphereFactory {
    private val radius = World.C1
    private val center = v4Point(World.C0, World.C0, World.C0)

    private val basePositions = listOf(
            v4Point( 0.0f,  0.0f, radius),
            v4Point( radius,  0.0f, 0.0f),
            v4Point( 0.0f,  radius, 0.0f),
            v4Point(-radius,  0.0f, 0.0f),
            v4Point( 0.0f, -radius, 0.0f)
    )

    private val baseIndices = shortArrayOf(
            0, 1, 2,
            0, 2, 3,
            0, 3, 4,
            0, 4, 1)

    fun generateFlatMeshZero(): AbcMeshRaw {
        val tmpNormal = Vector4()
        val rawAttributes = AbcVertexAttributesRaw(basePositions.size)
        rawAttributes.positionsBuffer.rewind()
        rawAttributes.normalsBuffer.rewind()
        basePositions.forEach {
            rawAttributes.positionsBuffer.putVector(it)
            rawAttributes.normalsBuffer.putVector(normalForPoint(it, tmpNormal))
        }
        return AbcMeshRaw(rawAttributes, baseIndices)
    }

    private fun normalForPoint(point: Vector4, out: Vector4 = Vector4()): Vector4 {
        out.set(center).negate()
                .add(point)
                .normalize()
        return out
    }
}