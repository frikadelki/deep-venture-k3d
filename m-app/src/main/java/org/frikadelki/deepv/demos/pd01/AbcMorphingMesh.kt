/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.demos.pd01

import org.frikadelki.deepv.common.*
import org.frikadelki.deepv.common.mesh.AbcVertexAttributesBaked
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.program.Program
import org.frikadelki.deepv.pipeline.program.ProgramSource
import org.frikadelki.deepv.pipeline.program.UniformHandle
import org.frikadelki.deepv.pipeline.program.VertexAttributeHandle
import java.nio.ShortBuffer

class AbcMorphingMeshFrame(val vertexAttributes: AbcVertexAttributesBaked) {
}

class AbcMorphingMesh(private val frames: List<AbcMorphingMeshFrame>,
                      val indexBuffer: ShortBuffer) {
    init {
        if (frames.isEmpty()) {
            throw IllegalArgumentException("Must have at least one frame.")
        }
    }

    fun last(): AbcMorphingMeshFrame {
        return frames[frames.size - 1]
    }
}

class AbcMeshMorphingLump(private val program: AbcMeshMorphingProgram,
                          private val mesh: AbcMorphingMesh,
                          private val colorDiffuse: Vector4,
                          private val colorSpecular: Vector4)
    : Lump {
    override fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext) {
        program.enable()
        program.setCamera(scene.camera)
        program.setLights(scene.lights)

        program.setModelMatrix(pawn.transform.modelMatrix)
        program.setModelColor(colorDiffuse, colorSpecular)

        program.setVertexAttributes(mesh.last().vertexAttributes)
        program.drawTriangles(mesh.indexBuffer)

        program.disable()
    }

    override fun onUpdateAnimations(deltaMillis: Long) {
    }
}

class AbcMeshMorphingProgram(val pipeline: Pipeline) {
    private val lightsSnippet = LightsSnippet(2)

    private val programSource = ProgramSource(
            """
                precision mediump float;

                uniform mat4 viewProjectionMatrix;

                uniform mat4 modelMatrix;

                attribute vec3 vPosition;
                attribute vec3 vNormal;

                varying vec3 varPosition;
                varying vec3 varNormal;

                void main() {
                    vec4 worldPosition = modelMatrix * vec4(vPosition, 1.0);
                    varPosition = vec3(worldPosition) / worldPosition.w;
                    varNormal = normalize(modelMatrix * vec4(vNormal, 0.0)).xyz;
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
    private val modelColorDiffuse: UniformHandle = program.uniform("modelColorDiffuse")
    private val modelColorSpecular: UniformHandle = program.uniform("modelColorSpecular")

    private val vertexPosition: VertexAttributeHandle = program.vertexAttribute("vPosition")
    private val vertexNormal: VertexAttributeHandle = program.vertexAttribute("vNormal")

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

    fun setModelMatrix(matrix: Matrix4) {
        modelMatrix.setMatrix(matrix)
    }

    fun setModelColor(colorDiffuse: Vector4, colorSpecular: Vector4) {
        modelColorDiffuse.setVector(colorDiffuse)
        modelColorSpecular.setVector(colorSpecular)
    }

    fun setVertexAttributes(attributes: AbcVertexAttributesBaked) {
        vertexPosition.enable()
        vertexNormal.enable()
        attributes.bind(vertexPosition, vertexNormal)
    }

    fun drawTriangles(indexBuffer: ShortBuffer) {
        program.drawTriangles(indexBuffer)
    }

    fun disable() {
        vertexPosition.disable()
        vertexNormal.disable()
    }

    fun dispose() {
        program.dispose()
    }
}