/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import android.opengl.GLES20
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.main.SceneLoop
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.v4AxisZ
import org.frikadelki.deepv.pipeline.math.v4Point
import org.frikadelki.deepv.pipeline.math.v4Vector


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

    private val cubeMesh = pd00CubeMesh()

    // scene pawns

    private val scubePawn = Pawn()
    private val scubeScaleFactor = 1.1f
    private val scubeRotationSpeed = 0.05f
    init {
        scubePawn.transform.selfScale(v4Point(scubeScaleFactor, scubeScaleFactor, scubeScaleFactor))
        scubePawn.transform.worldTranslate(v4Vector(y = 0.7f))

        val meshPainterLump = Pd0MeshPainterLump(
                mainProgram,
                cubeMesh,
                v4Vector(0.63671875f, 0.76953125f, 0.22265625f, 1.0f))
        scubePawn.addLump(meshPainterLump)
    }

    private val bubePawn = Pawn()
    init {
        bubePawn.transform.selfRotate(v4AxisZ(), 45.0f)
        bubePawn.transform.worldTranslate(v4Vector(x = -0.5f, z = 0.2f))

        val meshPainterLump = Pd0MeshPainterLump(
                mainProgram,
                cubeMesh,
                v4Vector(0.63671875f, 0.1f, 0.22265625f, 1.0f))
        bubePawn.addLump(meshPainterLump)
    }

    // scene

    private val scene = Scene()

    init {
        val eyePosition = v4Point(1.5f, 0.0f, 0.5f)
        val lookAtCenter = v4Point()
        val cameraUp = v4AxisZ()
        scene.camera.setLookAt(eyePosition, lookAtCenter, cameraUp)

        scene.addPawn(scubePawn)
        scene.addPawn(bubePawn)
    }

    // update & draw logic

    fun onUpdateAnimations(deltaMillis: Long) {
        val rotationAngle = deltaMillis * scubeRotationSpeed
        scubePawn.transform.selfRotate(v4AxisZ(), rotationAngle)
    }

    fun onDrawFrame() {
        pipeline.setClearColor(0.5f, 0.0f, 0.0f, 1.0f)
        pipeline.clearColorBuffer()
        // pipeline.setCullingEnabled(true)
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