/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.common.*
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import java.nio.FloatBuffer
import java.nio.ShortBuffer

fun pd00CubeMesh(): Pd00Mesh {
    val exportComponents = Vector4Components.THREE
    val mesh = rawCubeMesh()
    val vertexBuffer = mesh.positionsBuffer.toDirectFloatBuffer(exportComponents)
    return Pd00Mesh(vertexBuffer, exportComponents, mesh.indexBuffer)
}

data class Pd00Mesh(val positionsBuffer: FloatBuffer,
                    val positionsComponents: Vector4Components,
                   val indexBuffer: ShortBuffer)

class Pd0MeshPainterLump(private val program: Pd00Program,
                         private val mesh: Pd00Mesh,
                         private val color: Vector4) : Lump {
    override fun onDraw(pawn: Pawn, scene: Scene, context: DrawContext) {
        program.enable()
        program.setViewProjectionMatrix(scene.camera.viewProjectionMatrix)

        program.setModelMatrix(pawn.transform.modelMatrix)
        program.setVertexColor(color)
        program.setVertexPosition(mesh.positionsBuffer, mesh.positionsComponents)
        program.drawTriangles(mesh.indexBuffer)

        program.disable()
    }
}