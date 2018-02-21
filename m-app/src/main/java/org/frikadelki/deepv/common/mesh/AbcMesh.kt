/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.directShortBufferFromArray
import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.program.VertexAttributeHandle
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class AbcMeshBaked(val vertexAttributes: AbcVertexAttributesBaked,
                   val indexBuffer: ShortBuffer)

class AbcVertexAttributesBaked(private val positionsBuffer: FloatBuffer,
                               private val positionsComponents: Vector4Components,
                               private val normalsBuffer: FloatBuffer,
                               private val normalsComponents: Vector4Components) {
    fun bind(positionsHandle: VertexAttributeHandle, normalsHandle: VertexAttributeHandle) {
        positionsBuffer.rewind()
        positionsHandle.setData(positionsBuffer, positionsComponents, VertexAttributeHandle.ComponentType.FLOAT)
        normalsBuffer.rewind()
        normalsHandle.setData(normalsBuffer, normalsComponents, VertexAttributeHandle.ComponentType.FLOAT)
    }
}

class AbcMeshRaw(val vertexAttributes: AbcVertexAttributesRaw,
                 val indexBuffer: ShortArray) {
    class Recipe(val vertexAttributesRecipe: AbcVertexAttributesRaw.Recipe)

    constructor(verticesCount: Int, indicesCount: Int)
            : this(AbcVertexAttributesRaw(verticesCount), ShortArray(indicesCount))

    fun vertexCount(): Int {
        return vertexAttributes.vertexCount()
    }

    fun bake(recipe: Recipe): AbcMeshBaked {
        return AbcMeshBaked(
                vertexAttributes.bake(recipe.vertexAttributesRecipe),
                directShortBufferFromArray(indexBuffer))
    }
}

class AbcVertexAttributesRaw(val positionsBuffer: Vector4Array,
                             val normalsBuffer: Vector4Array) {
    class Recipe(val positionsComponents: Vector4Components,
                 val normalsComponents: Vector4Components)

    init {
        if (positionsBuffer.vectorsCount != normalsBuffer.vectorsCount) {
            throw IllegalArgumentException()
        }
    }

    constructor(verticesCount: Int)
            : this(Vector4Array(verticesCount), Vector4Array(verticesCount))

    fun vertexCount(): Int {
        return positionsBuffer.vectorsCount
    }

    fun bake(recipe: Recipe): AbcVertexAttributesBaked {
        return AbcVertexAttributesBaked(
                positionsBuffer.toDirectFloatBuffer(recipe.positionsComponents),
                recipe.positionsComponents,
                normalsBuffer.toDirectFloatBuffer(recipe.normalsComponents),
                recipe.normalsComponents)
    }

    fun copy(): AbcVertexAttributesRaw {
        val result = AbcVertexAttributesRaw(vertexCount())
        positionsBuffer.rewind()
        result.positionsBuffer.rewind()
        result.positionsBuffer.putAll(positionsBuffer)
        normalsBuffer.rewind()
        result.normalsBuffer.rewind()
        result.normalsBuffer.putAll(normalsBuffer)
        return result
    }
}