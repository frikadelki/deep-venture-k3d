/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import org.frikadelki.deepv.common.mesh.AbcMeshRaw
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesRaw
import org.frikadelki.deepv.pipeline.math.*


class AbcMorphingSphereFactory {
    private val radius = World.C1
    private val center = v4Point(World.C0, World.C0, World.C0)

    private val tetrahedronMeshFactory = AbcOctahedronMeshFactory()

    fun generateMorphingMesh(detailLevel: Int): AbcMorphingMesh {
        val flatMesh = generateFlatMesh(detailLevel)
        val sphereMeshAttributes = flatMesh.vertexAttributes.copy()
        arrangedOnSphere(sphereMeshAttributes)

        val flatBakedMesh = flatMesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE)))
        val sphereBakedMeshAttributes = sphereMeshAttributes.bake(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE))

        val morphingMesh = AbcMorphingMesh(flatBakedMesh.vertexAttributes, flatBakedMesh.indexBuffer)
        morphingMesh.addFrame(sphereBakedMeshAttributes, 6000)
        morphingMesh.addFrame(sphereBakedMeshAttributes, 3000)
        morphingMesh.addFrame(flatBakedMesh.vertexAttributes, 3000)
        morphingMesh.addFrame(flatBakedMesh.vertexAttributes, 3000)

        return morphingMesh
    }

    fun generateFlatMesh(detailLevel: Int): AbcMeshRaw {
        return tetrahedronMeshFactory.generateMesh(detailLevel)
    }

    fun generateSphericalMesh(detailLevel: Int): AbcMeshRaw {
        val mesh = tetrahedronMeshFactory.generateMesh(detailLevel)
        arrangedOnSphere(mesh.vertexAttributes)
        return mesh
    }

    private fun arrangedOnSphere(attributes: AbcVertexAttributesRaw) {
        val tmpNormal = Vector4()

        val positionsBuffer = attributes.positionsBuffer
        val normalsBuffer = attributes.normalsBuffer
        positionsBuffer.rewind()
        normalsBuffer.rewind()
        while (positionsBuffer.hasRemaining()) {
            positionsBuffer.replaceVector {
                normalForPoint(it, tmpNormal)
                normalsBuffer.putVector(tmpNormal)
                it.set(tmpNormal.scale(radius).point())
            }
        }
    }

    private fun normalForPoint(point: Vector4, out: Vector4 = Vector4()): Vector4 {
        out.set(center).negate().add(point).normalize()
        return out
    }
}

class AbcOctahedronMeshFactory {
    private val radius = World.C1
    private val center = v4Point(World.C0, World.C0, World.C0)

    private val pointPerTriangle = 3
    private val indicesPerTriangle = 3

    fun generateMesh(tessellationLevel: Int): AbcMeshRaw {
        if (tessellationLevel < 1) {
            throw IllegalArgumentException()
        }
        var mesh = generateBaseMesh()
        for(i in 1 until tessellationLevel) {
            mesh = tessellate(mesh)
        }
        return mesh
    }

    private fun tessellate(inMesh: AbcMeshRaw): AbcMeshRaw {
        val tmpVectorA = Vector4()
        val tmpVectorB = Vector4()

        val pointsPerTessellatedTriangle = 6
        val trianglesTessellationPattern = shortArrayOf(
                0, 3, 5,
                3, 1, 4,
                5, 3, 4,
                5, 4, 2
        )
        val trianglesTessellationFactor = trianglesTessellationPattern.size/indicesPerTriangle

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

            val a = outPositions[0].set(inPositions[aInIndex])
            val b = outPositions[1].set(inPositions[bInIndex])
            val c = outPositions[2].set(inPositions[cInIndex])

            val abv = tmpVectorA.set(a).negate().add(b)
            val bcv = tmpVectorB.set(b).negate().add(c)
            val normal = abv.cross(bcv).normalize()

            outPositions[3].set(abv).scale(0.5f).add(a).point()
            outPositions[4].set(bcv).scale(0.5f).add(b).point()
            outPositions[5].set(c).negate().add(a).scale(0.5f).add(c).point()

            for(i in 0 until pointsPerTessellatedTriangle) {
                outNormals.putVector(normal)
            }

            trianglesTessellationPattern.forEachIndexed { i, value ->
                outIndices[outIndicesOffset + i] = (outPositionIndexOffset + value).toShort()
            }
        }

        val inTrianglesCount = inMesh.indexBuffer.size / indicesPerTriangle

        val outVerticesCount = inTrianglesCount * pointsPerTessellatedTriangle
        val outTrianglesCount = inTrianglesCount * trianglesTessellationFactor

        val outMesh = AbcMeshRaw(outVerticesCount, outTrianglesCount * indicesPerTriangle)
        val outPositionsBuffer = outMesh.vertexAttributes.positionsBuffer
        val outNormalsBuffer = outMesh.vertexAttributes.normalsBuffer
        val outIndexBuffer = outMesh.indexBuffer

        for(i in 0 until inTrianglesCount) {
            val outPositionsOffset = i * pointsPerTessellatedTriangle
            val outPositionsSlice = outPositionsBuffer.slice(outPositionsOffset, pointsPerTessellatedTriangle)
            tessellateTriangle(
                    inMesh.vertexAttributes.positionsBuffer,
                    inMesh.indexBuffer, i * indicesPerTriangle,
                    outPositionsSlice,
                    outNormalsBuffer,
                    outIndexBuffer, i * trianglesTessellationPattern.size,
                    outPositionsOffset)
        }

        return outMesh
    }

    private fun generateBaseMesh(): AbcMeshRaw {
        val tmpVectorA = Vector4()
        val tmpVectorB = Vector4()

        val basePositions = listOf(
                v4Point( 0.0f,  0.0f, radius),
                v4Point( radius,  0.0f, 0.0f),
                v4Point( 0.0f,  radius, 0.0f),

                v4Point( 0.0f,  0.0f, radius),
                v4Point( 0.0f,  radius, 0.0f),
                v4Point(-radius,  0.0f, 0.0f),

                v4Point( 0.0f,  0.0f, radius),
                v4Point(-radius,  0.0f, 0.0f),
                v4Point( 0.0f, -radius, 0.0f),

                v4Point( 0.0f,  0.0f, radius),
                v4Point( 0.0f, -radius, 0.0f),
                v4Point( radius,  0.0f, 0.0f)
        )

        val baseIndices = shortArrayOf(
                0, 1, 2,
                3, 4, 5,
                6, 7, 8,
                9, 10, 11)

        fun generateTetrahedron(positions: Vector4Array, normals: Vector4Array) {
            positions.rewind()
            normals.rewind()
            basePositions.forEach {
                positions.putVector(it)
            }
            for (i in 0 until basePositions.size/pointPerTriangle) {
                val pA = basePositions[i + 0]
                val pB = basePositions[i + 1]
                val pC = basePositions[i + 2]

                val abv = tmpVectorA.set(pA).negate().add(pB)
                val bcv = tmpVectorB.set(pB).negate().add(pC)
                val normal = abv.cross(bcv).normalize()
                for(j in 0 until pointPerTriangle) {
                    normals.putVector(normal)
                }
            }
        }

        fun mirrorZ(inPositions: Vector4Array, inNormals: Vector4Array,
                    outPositions: Vector4Array, outNormals: Vector4Array) {
            inPositions.rewind()
            outPositions.rewind()
            inPositions.forEachRemaining {
                outPositions.putVector(it.x, it.y, -it.z, it.w)
            }
            inNormals.rewind()
            outNormals.rewind()
            inNormals.forEachRemaining {
                outNormals.putVector(it.x, it.y, -it.z, it.w)
            }
        }

        val tetrahedronVerticesCount = basePositions.size
        val octahedronVerticesCount = tetrahedronVerticesCount * 2
        val positionsBuffer = Vector4Array(octahedronVerticesCount)
        val normalBuffer = Vector4Array(octahedronVerticesCount)

        val topPositions = positionsBuffer.slice(0, tetrahedronVerticesCount)
        val topNormals = normalBuffer.slice(0, tetrahedronVerticesCount)
        generateTetrahedron(topPositions, topNormals)

        val bottomPositions = positionsBuffer.slice(tetrahedronVerticesCount, tetrahedronVerticesCount)
        val bottomNormals = normalBuffer.slice(tetrahedronVerticesCount, tetrahedronVerticesCount)
        mirrorZ(topPositions, topNormals, bottomPositions, bottomNormals)

        val rawAttributes = AbcVertexAttributesRaw(positionsBuffer, normalBuffer)
        val indices = ShortArray(baseIndices.size * 2)
        System.arraycopy(baseIndices, 0, indices, 0, baseIndices.size)
        for(i in 0 until baseIndices.size) {
            indices[baseIndices.size + i] = (tetrahedronVerticesCount + baseIndices[baseIndices.size - 1 - i]).toShort()
        }

        return AbcMeshRaw(rawAttributes, indices)
    }
}