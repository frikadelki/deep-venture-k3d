/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/22
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.math.*

private const val POLAR_SEGMENTS_COUNT_MIN = 3
private const val Z_SEGMENTS_COUNT_MIN = 1

class AbcCylinderMesh(private val circleSegmentsCount: Int,
                      private val zSegmentsCount: Int) {
    init {
        if (circleSegmentsCount < POLAR_SEGMENTS_COUNT_MIN || zSegmentsCount < Z_SEGMENTS_COUNT_MIN) {
            throw IllegalArgumentException()
        }
    }

    private val verticesCount = circleSegmentsCount * (zSegmentsCount + 1)
    private val trianglesPerZSegmentCount = circleSegmentsCount * 2
    private val trianglesCount = trianglesPerZSegmentCount * zSegmentsCount
    private val zSegmentIncrement = World.C1 / zSegmentsCount.toFloat()

    private val attributes = AbcVertexAttributesRaw(verticesCount)
    private val indices = ShortArray(trianglesCount * 3)

    init {
        var circlesSlicingOffset = 0

        var previousZSegmentPositions = attributes.positionsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
        var previousZSegmentNormals = attributes.normalsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
        var nextZSegmentPositions = previousZSegmentPositions
        var nextZSegmentNormals = previousZSegmentNormals

        generateCirclePositions(circleSegmentsCount, nextZSegmentPositions, nextZSegmentNormals)
        for(zSegment in 0 until zSegmentsCount) {
            circlesSlicingOffset += circleSegmentsCount

            previousZSegmentPositions = nextZSegmentPositions
            nextZSegmentPositions = attributes.positionsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
            previousZSegmentPositions.rewind()
            previousZSegmentPositions.forEachRemaining {
                nextZSegmentPositions.putVector(it.x, it.y, it.z + zSegmentIncrement, it.w)
            }

            previousZSegmentNormals = nextZSegmentNormals
            nextZSegmentNormals = attributes.normalsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
            previousZSegmentNormals.rewind()
            nextZSegmentNormals.putAll(previousZSegmentNormals)
        }

        val translateMeshDZ = -World.C1 / 2.0f
        attributes.positionsBuffer.forEachRemaining {
            it.translate(dz = translateMeshDZ)
        }

        var indicesOffset = 0
        var previousZSegmentOffset = 0
        var nextZSegmentOffset = previousZSegmentOffset

        for (zSegment in 0 until zSegmentsCount) {
            previousZSegmentOffset = nextZSegmentOffset
            nextZSegmentOffset += circleSegmentsCount

            for(i in 0 until circleSegmentsCount) {
                indices[indicesOffset++] = (nextZSegmentOffset + i).toShort()
                indices[indicesOffset++] = (previousZSegmentOffset + i).toShort()
                indices[indicesOffset++] = (nextZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()

                indices[indicesOffset++] = (nextZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()
                indices[indicesOffset++] = (previousZSegmentOffset + i).toShort()
                indices[indicesOffset++] = (previousZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()
            }
        }
    }

    private fun generateCirclePositions(polarSegmentsCount: Int,
                                        outPositions: Vector4Array,
                                        outNormals: Vector4Array) {
        if (outPositions.remaining() < polarSegmentsCount) {
            throw IllegalArgumentException()
        }
        val angleStep: Float = World.ARC_360 / polarSegmentsCount
        val stepMatrix = Matrix4().setE().rotate(World.axisZ, angleStep)
        val position = v4Point(x = World.C1 / 2.0f)
        val tmpPosition = v4Vector().set(position)

        for(i in 0 until polarSegmentsCount) {
            outPositions.putVector(position)
            outNormals.putVector(tmpPosition.setVector().vectorNormalize())

            stepMatrix.multiply(position, tmpPosition).pointWDivide()
            position.set(tmpPosition)
        }
    }

    fun bakeMesh(recipe: AbcMeshRaw.Recipe): AbcMeshBaked {
        val rawMesh = AbcMeshRaw(attributes, indices)
        return rawMesh.bake(recipe)
    }
}