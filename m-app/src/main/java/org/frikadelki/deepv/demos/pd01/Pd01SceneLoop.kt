/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import android.opengl.GLES20
import org.frikadelki.deepv.common.EmptyLump
import org.frikadelki.deepv.common.Lights
import org.frikadelki.deepv.common.Pawn
import org.frikadelki.deepv.common.Scene
import org.frikadelki.deepv.common.mesh.*
import org.frikadelki.deepv.main.SceneLoop
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.directFloatBuffer
import org.frikadelki.deepv.pipeline.math.*


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

    private val abcMorphingMeshProgram = AbcMorphingMeshProgram(pipeline)
    private val abcSkeletalMeshProgram = AbcSkeletalMeshProgram(pipeline)

    // scene

    private val scene = Scene()

    // camera setup

    private val cameraEyePosition = v4Point(1.9f, 0.0f, 0.7f)
    private val cameraLookAtCenter = v4Point()
    private val cameraUp = v4Vector().set(World.axisZ)

    init {
        scene.camera.setLookAt(cameraEyePosition, cameraLookAtCenter, cameraUp)
    }

    // lights setup

    private val sceneBulb0 = Lights.Point(
            v4Point(x = 2.5f, y = 0.7f, z = 2.0f),
            v4Color(0.5f, 0.5f, 0.5f))

    init {
        scene.lights.ambient.set(0.4f, 0.4f, 0.4f)
        scene.lights.add(sceneBulb0)
    }

    // scene pawns

    private val morphSphere = Pawn()

    init {
        val morphingMesh = Pd01Resources.morphingSphereDemo(5)
        morphSphere.addLump(AbcMorphingMeshLump(
                abcMorphingMeshProgram,
                morphingMesh,
                v4Color(0.8f, 0.2f, 0.4f),
                v4Color(0.5f, 0.5f, 0.5f, 20.0f)))

        val rotationAxis = v4Vector(1.0f, 1.0f, 1.0f).vectorNormalize()
        val rotationSpeed = 0.05f
        morphSphere.addLump(object: EmptyLump() {
            override fun onUpdateAnimations(deltaMillis: Long) {
                val rotationAngle = deltaMillis * rotationSpeed
                morphSphere.transform.selfRotate(rotationAxis, rotationAngle)
            }
        })

        morphSphere.transform
                .selfRotate(World.axisZ, 22.5f)
                .worldTranslate(v4Vector(x = -0.4f, y = 0.6f, z = 0.5f))
        scene.addPawn(morphSphere)
    }

    private val skeletalCylinder = Pawn()

    init {
        val skeletalMesh = Pd01Resources.skeletalCylinder(32, 3)
        skeletalCylinder.addLump(AbcSkeletalMeshLump(
                abcSkeletalMeshProgram,
                skeletalMesh,
                true,
                v4Color(0.8f, 0.8f, 0.4f),
                v4Color(0.5f, 0.5f, 0.5f, 20.0f)))

        val radius = 0.8f
        val height = 1.3f
        skeletalCylinder.transform
                .selfRotate(World.axisZ, 0.0f)
                .selfScale(v4Vector(radius, radius, height))
                .worldTranslate(v4Vector(x = 0.68f, y = -0.3f, z = -0.25f))
        scene.addPawn(skeletalCylinder)
    }

    // update & draw logic

    fun onUpdateAnimations(deltaMillis: Long) {
        scene.onUpdateAnimations(deltaMillis)
    }

    fun onDrawFrame() {
        pipeline.setClearColor(0.2f, 0.5f, 0.5f, 1.0f)
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
        abcMorphingMeshProgram.dispose()
        abcSkeletalMeshProgram.dispose()
    }
}

private object Pd01Resources {
    fun morphingSphereDemo(detailLevel: Int): AbcMorphingMesh {
        val octahedron = AbcTessellatedOctahedron(detailLevel)
        val sphereMeshAttributes = octahedron.mesh.vertexAttributes.copy()
        AbcSphereMath.arrangeOnSphere(sphereMeshAttributes)

        val flatBakedMesh = octahedron.mesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE)))
        val sphereBakedMeshAttributes = sphereMeshAttributes.bake(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE))

        val morphingMesh = AbcMorphingMesh(flatBakedMesh.vertexAttributes, flatBakedMesh.indexBuffer)
        morphingMesh.addFrame(sphereBakedMeshAttributes, 6000)
        morphingMesh.addFrame(sphereBakedMeshAttributes, 3000)
        morphingMesh.addFrame(flatBakedMesh.vertexAttributes, 3000)
        morphingMesh.addFrame(flatBakedMesh.vertexAttributes, 3000)

        return morphingMesh
    }

    fun skeletalCylinder(circleSegmentsCount: Int, zSegmentsCount: Int) : AbcSkeletalMesh {
        val cylinderMesh = AbcCylinder(circleSegmentsCount, zSegmentsCount)
                .mesh.bake(AbcMeshRaw.Recipe(AbcVertexAttributesRaw.Recipe(Vector4Components.THREE, Vector4Components.THREE)))
        val skeletalAttributes = AbcSkeletalVertexAttributes(
                cylinderMesh.vertexAttributes,
                directFloatBuffer(0), Vector4Components.ZERO)
        return AbcSkeletalMesh(skeletalAttributes, listOf(), cylinderMesh.indexBuffer)
    }
}