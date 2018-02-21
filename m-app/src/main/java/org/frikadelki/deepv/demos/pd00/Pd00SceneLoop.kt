/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import android.opengl.GLES20
import org.frikadelki.deepv.common.EmptyLump
import org.frikadelki.deepv.common.Lights
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

    private val meshPainterProgram = Pd00Program(pipeline)

    private val cubeMesh = abcCubeBakedMesh()

    // scene

    private val scene = Scene()

    // camera

    val cameraEyePosition = v4Point(1.6f, 0.0f, 0.7f)
    val cameraLookAtCenter = v4Point()
    val cameraUp = v4Vector().set(World.axisZ)

    init {
        scene.camera.setLookAt(cameraEyePosition, cameraLookAtCenter, cameraUp)
    }

    // lights

    private val sceneAmbientColor = v4Color(0.4f, 0.4f, 0.4f)
    private val sceneSun0 = Lights.Direct(
            v4Vector(x = -1.0f, z = 1.0f),
            v4Color(0.3f, 0.3f, 0.3f))
    private val sceneBulb0 = Lights.Point(
            v4Point(x = 2.0f, z = 2.0f),
            v4Color(0.5f, 0.5f, 0.5f))

    init {
        scene.lights.ambient.set(sceneAmbientColor)
        scene.lights.add(sceneSun0)
        scene.lights.add(sceneBulb0)
    }

    // scene pawns

    private val scubePawn = Pawn()
    init {
        val meshPainterLump = Pd00AbcMeshPainterLump(
                meshPainterProgram,
                cubeMesh,
                v4Color(0.63671875f, 0.76953125f, 0.22265625f, 1.0f),
                v4Color(0.9f, 0.7f, 0.1f, 3.5f))
        scubePawn.addLump(meshPainterLump)

        val scubeRotationSpeed = 0.05f
        scubePawn.addLump(object: EmptyLump() {
            override fun onUpdateAnimations(deltaMillis: Long) {
                val rotationAngle = deltaMillis * scubeRotationSpeed
                scubePawn.transform.selfRotate(World.axisZ, rotationAngle)
            }
        })

        val scaleFactor = 1.1f
        scubePawn.transform
                .selfScale(v4Point(scaleFactor, scaleFactor, scaleFactor))
                .worldTranslate(v4Vector(y = 0.7f))
        scene.addPawn(scubePawn)
    }

    private val bubePawn = Pawn()
    init {
        val meshPainterLump = Pd00AbcMeshPainterLump(
                meshPainterProgram,
                cubeMesh,
                v4Color(0.63671875f, 0.1f, 0.22265625f, 1.0f),
                v4Color(1.0f, 0.1f, 0.1f, 4.5f))
        bubePawn.addLump(meshPainterLump)

        bubePawn.transform
                .selfRotate(World.axisZ, 22.5f)
                .selfRotate(World.axisY, 22.5f)
                .worldTranslate(v4Vector(y = -0.6f, z = -0.2f))
        scene.addPawn(bubePawn)
    }

    // update & draw logic

    fun onUpdateAnimations(deltaMillis: Long) {
        scene.onUpdateAnimations(deltaMillis)
    }

    fun onDrawFrame() {
        pipeline.setClearColor(0.1f, 0.4f, 0.4f, 1.0f)
        pipeline.clearColorBuffer()

        pipeline.enableDepthTest(GLES20.GL_LEQUAL, 1.0f)
        pipeline.clearDepthBuffer()

        scene.onDraw()
    }

    fun onViewportSizeChange(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        scene.camera.setViewport(width, height)
    }

    fun onDispose() {
        meshPainterProgram.dispose()
    }
}

private fun abcCubeBakedMesh(): AbcMeshBaked {
    val exportComponents = Vector4Components.THREE
    val mesh = abcCubeMeshRaw()
    return mesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(exportComponents, exportComponents)))
}