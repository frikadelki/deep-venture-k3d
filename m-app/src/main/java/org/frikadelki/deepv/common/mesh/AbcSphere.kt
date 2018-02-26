/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/26
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.World

class AbcSphere(detailLevel: Int) {
    val mesh = AbcTessellatedOctahedron(detailLevel).mesh
    init {
        AbcSphereMath.arrangeOnSphere(mesh.vertexAttributes)
    }
}

object AbcSphereMath {
    private const val radius = World.C1
    private val center = World.center

    private val tmpNormal = Vector4()

    fun arrangeOnSphere(attributes: AbcVertexAttributesRaw) {
        val positionsBuffer = attributes.positionsBuffer
        val normalsBuffer = attributes.normalsBuffer
        positionsBuffer.rewind()
        normalsBuffer.rewind()
        positionsBuffer.forEachRemaining {
            normalForPoint(it, tmpNormal)
            normalsBuffer.putVector(tmpNormal)
            it.set(tmpNormal.scale(radius).setPoint())
        }
    }

    private fun normalForPoint(point: Vector4, out: Vector4 = Vector4()): Vector4 {
        out.set(center).negate().add(point).vectorNormalize()
        return out
    }
}