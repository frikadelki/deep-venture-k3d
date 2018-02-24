/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import org.frikadelki.deepv.common.*
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesBaked
import org.frikadelki.deepv.pipeline.CullMode
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.TriangleWinding
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.program.Program
import org.frikadelki.deepv.pipeline.program.ProgramSource
import org.frikadelki.deepv.pipeline.program.UniformHandle
import org.frikadelki.deepv.pipeline.program.VertexAttributeHandle
import java.nio.ShortBuffer

class AbcMorphingMesh(zeroFrameAttributes: AbcVertexAttributesBaked,
                      val indexBuffer: ShortBuffer) {
    class Frame(val vertexAttributes: AbcVertexAttributesBaked,
                val keyTimeMillis: Long)

    class Step(val first: Frame, val second: Frame) {
        fun timeHit(timeMillis: Long): Boolean {
            return first.keyTimeMillis <= timeMillis && timeMillis <= second.keyTimeMillis
        }
    }

    private val frames = mutableListOf(
            Frame(zeroFrameAttributes, 0))

    var lengthMillis: Long = 0
        private set

    fun addFrame(attributes: AbcVertexAttributesBaked, animateInMillis: Long) {
        if (animateInMillis <= 0) {
            throw IllegalArgumentException()
        }
        lengthMillis += animateInMillis
        frames.add(Frame(attributes, lengthMillis))
    }

    fun find(millis: Long): Step {
        val it = frames.iterator()
        var previous = it.next()
        var next = previous
        while (it.hasNext()) {
            previous = next
            next = it.next()
            if (millis <= next.keyTimeMillis) {
                break
            }
        }
        return Step(previous, next)
    }
}

class AbcMorphingMeshInterpolator(private val mesh: AbcMorphingMesh) {
    private var timeMillis: Long = 0

    var currentFrames = mesh.find(timeMillis)
        private set

    var interpolatedTime: Float = 0.0f
        private set

    fun update(deltaMillis: Long) {
        timeMillis += deltaMillis
        timeMillis %= mesh.lengthMillis
        if (!currentFrames.timeHit(timeMillis)) {
            currentFrames = mesh.find(timeMillis)
            if (!currentFrames.timeHit(timeMillis)) {
                throw IllegalStateException("Broken morphing mesh.")
            }
        }
        updateInterpolatedTime()
    }

    private fun updateInterpolatedTime() {
        val numerator = (timeMillis - currentFrames.first.keyTimeMillis).toFloat()
        val denominator = (currentFrames.second.keyTimeMillis - currentFrames.first.keyTimeMillis).toFloat()
        interpolatedTime = numerator / denominator
    }
}

class AbcMorphingMeshLump(private val program: AbcMorphingMeshProgram,
                          private val mesh: AbcMorphingMesh,
                          private val colorDiffuse: Vector4,
                          private val colorSpecular: Vector4)
    : Lump {

    private val morphingInterpolator = AbcMorphingMeshInterpolator(mesh)

    override fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext) {
        program.enable()
        program.setCamera(scene.camera)
        program.setLights(scene.lights)

        program.setModelTransform(pawn.transform)
        program.setModelColor(colorDiffuse, colorSpecular)

        program.setMorphingMesh(morphingInterpolator)
        program.drawTriangles(mesh.indexBuffer)

        program.disable()
    }

    override fun onUpdateAnimations(deltaMillis: Long) {
        morphingInterpolator.update(deltaMillis)
    }
}

class AbcMorphingMeshProgram(val pipeline: Pipeline) {
    private val lightsSnippet = LightsSnippet(2)

    private val programSource = ProgramSource(
            """
                precision mediump float;

                uniform mat4 viewProjectionMatrix;

                uniform mat4 modelMatrix;
                uniform mat4 normalsMatrix;

                uniform float vInterpolatedTime;
                attribute vec3 vPositionA;
                attribute vec3 vNormalA;
                attribute vec3 vPositionB;
                attribute vec3 vNormalB;

                varying vec3 varPosition;
                varying vec3 varNormal;

                void main() {
                    vec3 vPosition = mix(vPositionA, vPositionB, vInterpolatedTime);
                    vec3 vNormal = mix(vNormalA, vNormalB, vInterpolatedTime);

                    vec4 worldPosition = modelMatrix * vec4(vPosition, 1.0);
                    varPosition = vec3(worldPosition) / worldPosition.w;
                    varNormal = normalize(normalsMatrix * vec4(vNormal, 0.0)).xyz;
                    gl_Position = viewProjectionMatrix * worldPosition;
                }

            """.trimIndent(),
            """
                precision mediump float;

                ${lightsSnippet.fragmentSource}

                uniform vec4 cameraEyePosition;

                uniform vec4 modelColorDiffuse;
                uniform vec4 modelColorSpecular;

                varying vec3 varPosition;
                varying vec3 varNormal;

                void main() {
                    LightsIntensity light = lightsIntensity(varPosition, varNormal, cameraEyePosition.xyz, modelColorSpecular.w);
                    gl_FragColor = (light.ambient + light.diffuse) * modelColorDiffuse + light.specular * modelColorSpecular;
                }

            """.trimIndent())

    private val program: Program = pipeline.loadProgram(programSource)

    private val viewProjectionMatrix: UniformHandle = program.uniform("viewProjectionMatrix")
    private val cameraEyePosition: UniformHandle = program.uniform("cameraEyePosition")

    private val lightsBinding = lightsSnippet.makeBinding(program)

    private val modelMatrix: UniformHandle = program.uniform("modelMatrix")
    private val normalsMatrix: UniformHandle = program.uniform("normalsMatrix")
    private val modelColorDiffuse: UniformHandle = program.uniform("modelColorDiffuse")
    private val modelColorSpecular: UniformHandle = program.uniform("modelColorSpecular")

    private val vertexInterpolatedTime: UniformHandle = program.uniform("vInterpolatedTime")
    private val vertexPositionA: VertexAttributeHandle = program.vertexAttribute("vPositionA")
    private val vertexNormalA: VertexAttributeHandle = program.vertexAttribute("vNormalA")
    private val vertexPositionB: VertexAttributeHandle = program.vertexAttribute("vPositionB")
    private val vertexNormalB: VertexAttributeHandle = program.vertexAttribute("vNormalB")

    fun enable() {
        program.use()
    }

    fun setCamera(camera: Camera) {
        cameraEyePosition.setVector(camera.eyePosition)
        viewProjectionMatrix.setMatrix(camera.viewProjectionMatrix)
    }

    fun setLights(lights: Lights) {
        lightsBinding.setLights(lights)
    }

    fun setModelTransform(transform: Transform) {
        modelMatrix.setMatrix(transform.modelMatrix)
        normalsMatrix.setMatrix(transform.normalsMatrix)
    }

    fun setModelColor(colorDiffuse: Vector4, colorSpecular: Vector4) {
        modelColorDiffuse.setVector(colorDiffuse)
        modelColorSpecular.setVector(colorSpecular)
    }

    fun setMorphingMesh(interpolator: AbcMorphingMeshInterpolator) {
        vertexInterpolatedTime.setFloat(interpolator.interpolatedTime)

        vertexPositionA.enable()
        vertexNormalA.enable()
        interpolator.currentFrames.first
                .vertexAttributes.bind(vertexPositionA, vertexNormalA)

        vertexPositionB.enable()
        vertexNormalB.enable()
        interpolator.currentFrames.second
                .vertexAttributes.bind(vertexPositionB, vertexNormalB)
    }

    fun drawTriangles(indexBuffer: ShortBuffer) {
        pipeline.setCullingEnabled(true)
        pipeline.setCullFace(CullMode.BACK)
        pipeline.setFrontFace(TriangleWinding.COUNTERCLOCKWISE)
        program.drawTriangles(indexBuffer)
    }

    fun disable() {
        vertexPositionA.disable()
        vertexNormalA.disable()
        vertexPositionB.disable()
        vertexNormalB.disable()
    }

    fun dispose() {
        program.dispose()
    }
}