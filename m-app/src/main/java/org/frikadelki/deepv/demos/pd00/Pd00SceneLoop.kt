/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import android.opengl.GLES20
import org.frikadelki.deepv.common.Lights
import org.frikadelki.deepv.common.EmptyLump
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.common.mesh.AbcMeshBaked
import org.frikadelki.deepv.common.mesh.AbcMeshRaw
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesRaw
import org.frikadelki.deepv.common.mesh.abcCubeMeshRaw
import org.frikadelki.deepv.main.SceneLoop
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.*


class Pd00SceneLoop : SceneLoop {
    private var scene: Pd00Scene? = null

    override fun onPipelineCreated(pipeline: Pipeline) {
        scene?.onDispose()
        scene = Pd00Scene(pipeline)
    }

    override fun onPipelineDisposed() {
        scene?.onDispose()
        scene = null
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        scene?.onViewportSizeChange(width, height)
    }

    override fun onUpdateAnimations(deltaMillis: Long) {
        scene?.onUpdateAnimations(deltaMillis)
    }

    override fun onDrawFrame() {
        scene?.onDrawFrame()
    }
}

private class Pd00Scene(val pipeline: Pipeline) {
    // shared resources
    private val mainProgram = Pd00Program(pipeline)

    private val cubeMesh = abcCubeBakedMesh()

    // scene pawns

    private val scubePawn = Pawn()
    private val scubeScaleFactor = 1.1f
    private val scubeRotationSpeed = 0.05f
    init {
        scubePawn.transform.selfScale(v4Point(scubeScaleFactor, scubeScaleFactor, scubeScaleFactor))
        scubePawn.transform.worldTranslate(v4Vector(y = 0.7f))

        val meshPainterLump = Pd00AbcMeshPainterLump(
                mainProgram,
                cubeMesh,
                v4Color(0.63671875f, 0.76953125f, 0.22265625f, 1.0f),
                v4Color(0.9f, 0.7f, 0.1f, 3.5f))
        scubePawn.addLump(meshPainterLump)
        scubePawn.addLump(object: EmptyLump() {
            override fun onUpdateAnimations(deltaMillis: Long) {
                val rotationAngle = deltaMillis * scubeRotationSpeed
                scubePawn.transform.selfRotate(v4AxisZ(), rotationAngle)
            }
        })
    }

    private val bubePawn = Pawn()
    init {
        bubePawn.transform.selfRotate(v4AxisZ(), 15.0f)
        bubePawn.transform.worldTranslate(v4Vector(x = -0.5f, z = 0.2f))

        val meshPainterLump = Pd00AbcMeshPainterLump(
                mainProgram,
                cubeMesh,
                v4Color(0.63671875f, 0.1f, 0.22265625f, 1.0f),
                v4Color(1.0f, 0.1f, 0.1f, 4.5f))
        bubePawn.addLump(meshPainterLump)
    }

    // scene

    private val scene = Scene()

    val cameraEyePosition = v4Point(1.6f, 0.0f, 0.7f)
    val cameraLookAtCenter = v4Point()
    val cameraUp = v4AxisZ()

    private val sceneAmbientColor = v4Color(0.4f, 0.4f, 0.4f)
    private val sceneSun0 = Lights.Direct(
            v4Vector(x = -1.0f, z = 1.0f),
            v4Color(0.3f, 0.3f, 0.3f))
    private val sceneBulb0 = Lights.Point(
            v4Point(x = 2.0f, z = 2.0f),
            v4Color(0.5f, 0.5f, 0.5f))

    init {
        scene.camera.setLookAt(cameraEyePosition, cameraLookAtCenter, cameraUp)

        scene.lights.ambient.set(sceneAmbientColor)
        scene.lights.add(sceneSun0)
        scene.lights.add(sceneBulb0)

        scene.addPawn(scubePawn)
        scene.addPawn(bubePawn)
    }

    // update & draw logic

    fun onUpdateAnimations(deltaMillis: Long) {
        scene.onUpdateAnimations(deltaMillis)
    }

    fun onDrawFrame() {
        pipeline.setClearColor(0.1f, 0.4f, 0.4f, 1.0f)
        pipeline.clearColorBuffer()
        pipeline.setCullingEnabled(true)

        scene.onDraw()
    }

    fun onViewportSizeChange(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        scene.camera.setViewport(width, height)
    }

    fun onDispose() {
        mainProgram.dispose()
    }
}

private fun abcCubeBakedMesh(): AbcMeshBaked {
    val exportComponents = Vector4Components.THREE
    val mesh = abcCubeMeshRaw()
    return mesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(exportComponents, exportComponents)))
}