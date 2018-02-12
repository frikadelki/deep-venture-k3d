/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/11
 */

package org.frikadelki.deepv.scene

import org.frikadelki.deepv.pipeline.directShortBuffer
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.math.World
import org.frikadelki.deepv.pipeline.math.v4AxisX
import java.nio.ShortBuffer

fun primitiveCube(): SimplestPrimitive {
    val sideSize: Float = World.C1
    val exportComponents = Vector4Components.THREE

    val sidesCount = 4
    val pointsPerSide = 4
    val sideIndicesTemplate = shortArrayOf(0, 1, 2, 0, 2, 3)

    val tmpMatrix = Matrix4()
    val tmpSideIndicesArray = ShortArray(sideIndicesTemplate.size)

    val verticesPositionsBuffer = Vector4Array(sidesCount * pointsPerSide)
    val geometryIndexBuffer = directShortBuffer(sidesCount * sideIndicesTemplate.size)
    var planeIndex = 0

    fun generateSide(vertexPositionArray: Vector4Array, indexBufferOutput: ShortBuffer, indexOffset: Int) {
        vertexPositionArray.putPoint(World.C0, World.C0, World.C0)
        vertexPositionArray.putPoint(sideSize, World.C0, World.C0)
        vertexPositionArray.putPoint(sideSize, sideSize, World.C0)
        vertexPositionArray.putPoint(World.C0, sideSize, World.C0)
        for ((index, value) in sideIndicesTemplate.withIndex()) {
            tmpSideIndicesArray[index] = (indexOffset + value).toShort()
        }
        indexBufferOutput.put(tmpSideIndicesArray)
    }

    fun addPlane(transformer: (matrix: Matrix4) -> Matrix4) {
        if (planeIndex < 0 || planeIndex >= sidesCount) {
            throw IllegalStateException("Plain index out of bounds.")
        }

        val indexOffset = planeIndex * pointsPerSide
        val planeVertexBufferSlice = verticesPositionsBuffer.slice(indexOffset, pointsPerSide)
        generateSide(planeVertexBufferSlice, geometryIndexBuffer, indexOffset)

        val transformMatrix = transformer.invoke(tmpMatrix.setE())
        planeVertexBufferSlice.multiplyAll(transformMatrix)
        planeVertexBufferSlice.perspectiveDivideAll()

        planeIndex++
    }

    // top
    addPlane { matrix -> matrix
            .translate(dz = sideSize)
    }

    // bottom
    addPlane { matrix -> matrix
            .translate(dy = sideSize)
            .rotate(v4AxisX(), World.ARC_180)
    }

    // left
    addPlane { matrix -> matrix
            .rotate(v4AxisX(), World.ARC_090)
    }

    // right
    addPlane { matrix -> matrix
            .translate(dy = sideSize, dz = sideSize)
            .rotate(v4AxisX(), World.ARC_270)
    }

    // center on origin
    val dToOrigin = -sideSize/2.0f
    verticesPositionsBuffer.multiplyAll(tmpMatrix.setE().translate(dToOrigin, dToOrigin, dToOrigin))
    verticesPositionsBuffer.perspectiveDivideAll()

    // export
    val vertexBuffer = verticesPositionsBuffer.toDirectFloatBuffer(exportComponents)
    return SimplestPrimitive(vertexBuffer, exportComponents, geometryIndexBuffer)
}