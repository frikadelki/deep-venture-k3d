/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common.mesh

import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.program.VertexAttributeHandle
import java.nio.FloatBuffer
import java.nio.ShortBuffer

data class AbcMeshBaked(val vertexAttributes: AbcVertexAttributesBaked,
                        val indexBuffer: ShortBuffer)

data class AbcVertexAttributesBaked(private val positionsBuffer: FloatBuffer,
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

data class AbcMeshRaw(val vertexAttributes: AbcVertexAttributesRaw,
                      val indexBuffer: ShortBuffer) {
    data class Recipe(val vertexAttributesRecipe: AbcVertexAttributesRaw.Recipe)

    fun bake(recipe: Recipe): AbcMeshBaked {
        return AbcMeshBaked(
                vertexAttributes.bake(recipe.vertexAttributesRecipe),
                indexBuffer)
    }
}

data class AbcVertexAttributesRaw(val positionsBuffer: Vector4Array,
                                  val normalsBuffer: Vector4Array) {
    data class Recipe(val positionsComponents: Vector4Components,
                      val normalsComponents: Vector4Components)

    fun bake(recipe: Recipe): AbcVertexAttributesBaked {
        return AbcVertexAttributesBaked(
                positionsBuffer.toDirectFloatBuffer(recipe.positionsComponents),
                recipe.positionsComponents,
                normalsBuffer.toDirectFloatBuffer(recipe.normalsComponents),
                recipe.normalsComponents)
    }
}