/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/26
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.INDICES_PER_TRIANGLE
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Array

fun tessellateMeshWithPrimitiveMedianDivision(inMesh: AbcMeshRaw): AbcMeshRaw {
    val tmpVectorA = Vector4()
    val tmpVectorB = Vector4()

    val pointsPerTessellatedTriangle = 6
    val trianglesTessellationPattern = shortArrayOf(
            0, 3, 5,
            3, 1, 4,
            5, 3, 4,
            5, 4, 2
    )
    val trianglesTessellationFactor = trianglesTessellationPattern.size / INDICES_PER_TRIANGLE

    fun tessellateTriangle(inPositions: Vector4Array,
                           inTriangleIndices: ShortArray,
                           inTriangleIndicesOffset: Int,
                           outPositions: Vector4Array,
                           outNormals: Vector4Array,
                           outIndices: ShortArray,
                           outIndicesOffset: Int,
                           outPositionIndexOffset: Int) {
        val aInIndex = inTriangleIndices[inTriangleIndicesOffset + 0].toInt()
        val bInIndex = inTriangleIndices[inTriangleIndicesOffset + 1].toInt()
        val cInIndex = inTriangleIndices[inTriangleIndicesOffset + 2].toInt()


        val a = outPositions.putVector(inPositions.slice(aInIndex))
        val b = outPositions.putVector(inPositions.slice(bInIndex))
        val c = outPositions.putVector(inPositions.slice(cInIndex))

        val abv = tmpVectorA.set(a).negate().add(b)
        val bcv = tmpVectorB.set(b).negate().add(c)
        val normal = abv.cross(bcv).vectorNormalize()

        outPositions.nextSlice().set(abv).scale(0.5f).add(a).setPoint()
        outPositions.nextSlice().set(bcv).scale(0.5f).add(b).setPoint()
        outPositions.nextSlice().set(c).negate().add(a).scale(0.5f).add(c).setPoint()

        for(i in 0 until pointsPerTessellatedTriangle) {
            outNormals.putVector(normal)
        }

        trianglesTessellationPattern.forEachIndexed { i, value ->
            outIndices[outIndicesOffset + i] = (outPositionIndexOffset + value).toShort()
        }
    }

    val inTrianglesCount = inMesh.indexBuffer.size / INDICES_PER_TRIANGLE

    val outVerticesCount = inTrianglesCount * pointsPerTessellatedTriangle
    val outTrianglesCount = inTrianglesCount * trianglesTessellationFactor

    val outMesh = AbcMeshRaw(outVerticesCount, outTrianglesCount * INDICES_PER_TRIANGLE)
    val outPositionsBuffer = outMesh.vertexAttributes.positionsBuffer
    val outNormalsBuffer = outMesh.vertexAttributes.normalsBuffer
    val outIndexBuffer = outMesh.indexBuffer

    for(i in 0 until inTrianglesCount) {
        val outPositionsOffset = i * pointsPerTessellatedTriangle
        val outPositionsSlice = outPositionsBuffer.slice(outPositionsOffset, pointsPerTessellatedTriangle)
        tessellateTriangle(
                inMesh.vertexAttributes.positionsBuffer,
                inMesh.indexBuffer, i * INDICES_PER_TRIANGLE,
                outPositionsSlice,
                outNormalsBuffer,
                outIndexBuffer, i * trianglesTessellationPattern.size,
                outPositionsOffset)
    }

    return outMesh
}