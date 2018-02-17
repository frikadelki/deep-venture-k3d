/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/15
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.math.*

fun abcCubeMeshRaw(): AbcMeshRaw {
    val sideSize: Float = World.C1

    val sidesCount = 6
    val pointsPerSide = 4
    val sideIndicesTemplate = shortArrayOf(0, 1, 2, 0, 2, 3)

    val tmpMatrix = Matrix4()

    val positionsBuffer = Vector4Array(sidesCount * pointsPerSide)
    val normalsBuffer = Vector4Array(sidesCount * pointsPerSide)
    val indexBuffer = ShortArray(sidesCount * sideIndicesTemplate.size)
    var planeIndex = 0

    fun generatePlaneMesh(positionsArray: Vector4Array,
                          normalsArray: Vector4Array,
                          indexBufferOutput: ShortArray,
                          indexOffset: Int,
                          positionIndexOffset: Int) {
        positionsArray.putPoint(World.C0, World.C0, World.C0)
        positionsArray.putPoint(sideSize, World.C0, World.C0)
        positionsArray.putPoint(sideSize, sideSize, World.C0)
        positionsArray.putPoint(World.C0, sideSize, World.C0)
        for(i in 0 until pointsPerSide) {
            normalsArray.putVector(World.C0, World.C0, World.C1, World.C0)
        }
        for ((index, value) in sideIndicesTemplate.withIndex()) {
            indexBufferOutput[indexOffset + index] = (positionIndexOffset + value).toShort()
        }
    }

    fun addSidePlane(transformer: (matrix: Matrix4) -> Matrix4) {
        if (planeIndex < 0 || planeIndex >= sidesCount) {
            throw IllegalStateException("Plain index out of bounds.")
        }

        val positionIndexOffset = planeIndex * pointsPerSide
        val planePositionsBufferSlice = positionsBuffer.slice(positionIndexOffset, pointsPerSide)
        val planeNormalsBufferSlice = normalsBuffer.slice(positionIndexOffset, pointsPerSide)
        generatePlaneMesh(
                planePositionsBufferSlice,
                planeNormalsBufferSlice,
                indexBuffer,
                planeIndex * sideIndicesTemplate.size,
                positionIndexOffset)

        val transformMatrix = transformer.invoke(tmpMatrix.setE())
        planePositionsBufferSlice.multiplyAll(transformMatrix)
        planePositionsBufferSlice.perspectiveDivideAll()
        planeNormalsBufferSlice.multiplyAll(transformMatrix)
        planeNormalsBufferSlice.normalizeAll()

        planeIndex++
    }

    // top
    addSidePlane { matrix -> matrix
            .translate(dz = sideSize)
    }

    // bottom
    addSidePlane { matrix -> matrix
            .translate(dy = sideSize)
            .rotate(v4AxisX(), World.ARC_180)
    }

    // left
    addSidePlane { matrix -> matrix
            .rotate(v4AxisX(), World.ARC_090)
    }

    // right
    addSidePlane { matrix -> matrix
            .translate(dy = sideSize, dz = sideSize)
            .rotate(v4AxisX(), World.ARC_270)
    }

    // front
    addSidePlane { matrix -> matrix
            .translate(dx = sideSize, dz = sideSize)
            .rotate(v4AxisY(), World.ARC_090)
    }

    // back
    addSidePlane { matrix -> matrix
            .rotate(v4AxisY(), World.ARC_270)
    }

    // center on origin
    val dToOrigin = -sideSize/2.0f
    tmpMatrix.setE().translate(dToOrigin, dToOrigin, dToOrigin)
    positionsBuffer.multiplyAll(tmpMatrix)
    positionsBuffer.perspectiveDivideAll()
    normalsBuffer.multiplyAll(tmpMatrix)
    normalsBuffer.normalizeAll()

    return AbcMeshRaw(AbcVertexAttributesRaw(positionsBuffer, normalsBuffer), indexBuffer)
}