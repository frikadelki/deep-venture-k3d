/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.common.Lump
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.common.rawCubeMesh
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import java.nio.FloatBuffer
import java.nio.ShortBuffer

fun pd00CubeMesh(): Pd00Mesh {
    val exportComponents = Vector4Components.THREE
    val mesh = rawCubeMesh()
    val positionsBuffer = mesh.positionsBuffer.toDirectFloatBuffer(exportComponents)
    val normalsBuffer = mesh.normalsBuffer.toDirectFloatBuffer(exportComponents)
    return Pd00Mesh(
            positionsBuffer, exportComponents,
            normalsBuffer, exportComponents,
            mesh.indexBuffer)
}

data class Pd00Mesh(val positionsBuffer: FloatBuffer,
                    val positionsComponents: Vector4Components,
                    val normalsBuffer: FloatBuffer,
                    val normalComponents: Vector4Components,
                    val indexBuffer: ShortBuffer)

class Pd0MeshPainterLump(private val program: Pd00Program,
                         private val mesh: Pd00Mesh,
                         private val colorDiffuse: Vector4,
                         private val colorSpecular: Vector4) : Lump {

    override fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext) {
        program.enable()
        program.setCamera(scene.camera)
        program.setLights(scene.lights)

        program.setModelMatrix(pawn.transform.modelMatrix)
        program.setModelColor(colorDiffuse, colorSpecular)

        program.setVertexPosition(mesh.positionsBuffer, mesh.positionsComponents)
        program.setVertexNormals(mesh.normalsBuffer, mesh.normalComponents)
        program.drawTriangles(mesh.indexBuffer)

        program.disable()
    }
}