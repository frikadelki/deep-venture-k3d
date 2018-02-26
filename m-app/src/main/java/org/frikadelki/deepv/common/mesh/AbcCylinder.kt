/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/22
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.INDICES_PER_TRIANGLE
import org.frikadelki.deepv.pipeline.math.*

private const val POLAR_SEGMENTS_COUNT_MIN = 3
private const val Z_SEGMENTS_COUNT_MIN = 1

class AbcCylinder(private val circleSegmentsCount: Int,
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

    val mesh = AbcMeshRaw(verticesCount, trianglesCount * INDICES_PER_TRIANGLE)

    init {
        var circlesSlicingOffset = 0

        var previousZSegmentPositions = mesh.vertexAttributes.positionsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
        var previousZSegmentNormals = mesh.vertexAttributes.normalsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
        var nextZSegmentPositions = previousZSegmentPositions
        var nextZSegmentNormals = previousZSegmentNormals

        generateCirclePositions(circleSegmentsCount, nextZSegmentPositions, nextZSegmentNormals)
        for(zSegment in 0 until zSegmentsCount) {
            circlesSlicingOffset += circleSegmentsCount

            previousZSegmentPositions = nextZSegmentPositions
            nextZSegmentPositions = mesh.vertexAttributes.positionsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
            previousZSegmentPositions.rewind()
            previousZSegmentPositions.writeRemainingTo(nextZSegmentPositions) {
                it.z += zSegmentIncrement
            }

            previousZSegmentNormals = nextZSegmentNormals
            nextZSegmentNormals = mesh.vertexAttributes.normalsBuffer.slice(circlesSlicingOffset, circleSegmentsCount)
            previousZSegmentNormals.rewind()
            previousZSegmentNormals.writeRemainingTo(nextZSegmentNormals)
        }

        val translateMeshDZ = -World.C1 / 2.0f
        mesh.vertexAttributes.positionsBuffer.forEachRemaining {
            it.translate(dz = translateMeshDZ)
        }

        var indicesOffset = 0
        var previousZSegmentOffset = 0
        var nextZSegmentOffset = previousZSegmentOffset

        for (zSegment in 0 until zSegmentsCount) {
            previousZSegmentOffset = nextZSegmentOffset
            nextZSegmentOffset += circleSegmentsCount

            for(i in 0 until circleSegmentsCount) {
                mesh.indexBuffer[indicesOffset++] = (nextZSegmentOffset + i).toShort()
                mesh.indexBuffer[indicesOffset++] = (previousZSegmentOffset + i).toShort()
                mesh.indexBuffer[indicesOffset++] = (nextZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()

                mesh.indexBuffer[indicesOffset++] = (nextZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()
                mesh.indexBuffer[indicesOffset++] = (previousZSegmentOffset + i).toShort()
                mesh.indexBuffer[indicesOffset++] = (previousZSegmentOffset + (i + 1) % circleSegmentsCount).toShort()
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
}