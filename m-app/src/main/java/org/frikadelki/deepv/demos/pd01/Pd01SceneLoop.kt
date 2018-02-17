/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import android.opengl.GLES20
import org.frikadelki.deepv.common.Lights
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.common.mesh.AbcMeshRaw
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesRaw
import org.frikadelki.deepv.main.SceneLoop
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.math.v4AxisZ
import org.frikadelki.deepv.pipeline.math.v4Color
import org.frikadelki.deepv.pipeline.math.v4Point


class Pd01SceneLoop : SceneLoop {
    private var scene: Pd01Scene? = null

    override fun onPipelineCreated(pipeline: Pipeline) {
        scene?.onDispose()
        scene = Pd01Scene(pipeline)
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

private class Pd01Scene(val pipeline: Pipeline) {
    // shared resources
    private val abcMeshMorphingProgram = AbcMeshMorphingProgram(pipeline)

    // scene pawns

    private val morphSphere = Pawn()
    init {
        val sphereFactory = AbcMorphingSphereFactory()
        val rawMesh = sphereFactory.generateSphericalMesh(5)

        val bakedMesh = rawMesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE)))
        val morphingMesh = AbcMorphingMesh(
                listOf(AbcMorphingMeshFrame(bakedMesh.vertexAttributes)),
                bakedMesh.indexBuffer)

        morphSphere.addLump(AbcMeshMorphingLump(
                abcMeshMorphingProgram,
                morphingMesh,
                v4Color(0.8f, 0.2f, 0.4f),
                v4Color(0.5f, 0.5f, 0.5f, 20.0f)))
    }

    // scene

    private val scene = Scene()

    private val cameraEyePosition = v4Point(1.6f, 0.0f, 0.7f)
    private val cameraLookAtCenter = v4Point()
    private val cameraUp = v4AxisZ()
    init {
        scene.camera.setLookAt(cameraEyePosition, cameraLookAtCenter, cameraUp)
    }

    private val sceneBulb0 = Lights.Point(
            v4Point(x = 2.0f, y = 0.2f, z = 2.0f),
            v4Color(0.5f, 0.5f, 0.5f))
    init {
        scene.lights.ambient.set(0.4f, 0.4f, 0.4f)
        scene.lights.add(sceneBulb0)
    }

    init {
        scene.addPawn(morphSphere)
    }

    // update & draw logic

    fun onUpdateAnimations(deltaMillis: Long) {
        scene.onUpdateAnimations(deltaMillis)
    }

    fun onDrawFrame() {
        pipeline.setClearColor(0.2f, 0.5f, 0.5f, 1.0f)
        pipeline.clearColorBuffer()
        pipeline.setCullingEnabled(true)

        scene.onDraw()
    }

    fun onViewportSizeChange(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        scene.camera.setViewport(width, height)
    }

    fun onDispose() {
        abcMeshMorphingProgram.dispose()
    }
}