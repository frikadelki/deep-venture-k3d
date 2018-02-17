/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.common.Lump
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.common.mesh.AbcMeshBaked
import org.frikadelki.deepv.pipeline.math.Vector4

class Pd00AbcMeshPainterLump(private val program: Pd00Program,
                             private val mesh: AbcMeshBaked,
                             private val colorDiffuse: Vector4,
                             private val colorSpecular: Vector4) : Lump {
    override fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext) {
        program.enable()
        program.setCamera(scene.camera)
        program.setLights(scene.lights)

        program.setModelMatrix(pawn.transform.modelMatrix)
        program.setModelColor(colorDiffuse, colorSpecular)

        program.setVertexAttributes(mesh.vertexAttributes)
        program.drawTriangles(mesh.indexBuffer)

        program.disable()
    }
}