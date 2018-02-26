/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/24
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.POINTS_PER_TRIANGLE
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.math.v4Point

class AbcTessellatedOctahedron(private val tessellationLevel: Int) {
    init {
        if (tessellationLevel < 0) {
            throw IllegalArgumentException()
        }
    }

    private val originalOctahedron = AbcOctahedron()

    val mesh: AbcMeshRaw

    init {
        var tmpMesh = originalOctahedron.mesh
        for(i in 1 until tessellationLevel) {
            tmpMesh = tessellateMeshWithPrimitiveMedianDivision(tmpMesh)
        }
        mesh = tmpMesh
    }
}

class AbcOctahedron {
    private val verticesCount = TetrahedronTop.verticesCount * 2
    private val indicesCount = TetrahedronTop.indicesCount * 2

    val mesh = AbcMeshRaw(verticesCount, indicesCount)

    init {
        val topPositions = mesh.vertexAttributes.positionsBuffer.slice(0, TetrahedronTop.verticesCount)
        val topNormals = mesh.vertexAttributes.normalsBuffer.slice(0, TetrahedronTop.verticesCount)
        TetrahedronTop.generate(topPositions, topNormals)
        // copy top indices as is
        System.arraycopy(TetrahedronTop.indices, 0, mesh.indexBuffer, 0, TetrahedronTop.indicesCount)

        val bottomPositions = mesh.vertexAttributes.positionsBuffer.slice(TetrahedronTop.verticesCount, TetrahedronTop.verticesCount)
        val bottomNormals = mesh.vertexAttributes.normalsBuffer.slice(TetrahedronTop.verticesCount, TetrahedronTop.verticesCount)
        topPositions.rewind()
        topPositions.writeRemainingTo(bottomPositions) { it.z = -it.z }
        topNormals.rewind()
        topNormals.writeRemainingTo(bottomNormals) { it.z = -it.z }
        // reverse direction for bottom indices
        for(i in 0 until TetrahedronTop.indicesCount) {
            val index = TetrahedronTop.indices[TetrahedronTop.indicesCount - 1 - i]
            mesh.indexBuffer[TetrahedronTop.indicesCount + i] = (TetrahedronTop.verticesCount + index).toShort()
        }
    }
}

private object TetrahedronTop {
    private val positions = listOf(
            v4Point( 0.0f,  0.0f, 1.0f),
            v4Point( 1.0f,  0.0f, 0.0f),
            v4Point( 0.0f,  1.0f, 0.0f),

            v4Point( 0.0f,  0.0f, 1.0f),
            v4Point( 0.0f,  1.0f, 0.0f),
            v4Point(-1.0f,  0.0f, 0.0f),

            v4Point( 0.0f,  0.0f, 1.0f),
            v4Point(-1.0f,  0.0f, 0.0f),
            v4Point( 0.0f, -1.0f, 0.0f),

            v4Point( 0.0f,  0.0f, 1.0f),
            v4Point( 0.0f, -1.0f, 0.0f),
            v4Point( 1.0f,  0.0f, 0.0f))

    val verticesCount = positions.size

    val indices = shortArrayOf(
            0, 1, 2,
            3, 4, 5,
            6, 7, 8,
            9, 10, 11)

    val indicesCount = indices.size

    fun generate(positions: Vector4Array, normals: Vector4Array) {
        val tmpVectorA = Vector4()
        val tmpVectorB = Vector4()
        val tmpNormal = Vector4()

        this.positions.forEach {
            positions.putVector(it)
        }

        // NOTE! this normals generation assumes that triangles vertices are
        // sequential in positions buffer
        for (i in 0 until verticesCount /POINTS_PER_TRIANGLE) {
            val pA = this.positions[i + 0]
            val pB = this.positions[i + 1]
            val pC = this.positions[i + 2]

            val vAB = tmpVectorA.set(pA).negate().add(pB)
            val vBC = tmpVectorB.set(pB).negate().add(pC)
            val normal = vAB.cross(vBC, tmpNormal).vectorNormalize()
            for(j in 0 until POINTS_PER_TRIANGLE) {
                normals.putVector(normal)
            }
        }
    }
}