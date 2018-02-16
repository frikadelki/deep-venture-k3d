/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.common.Camera
import org.frikadelki.deepv.common.Lights
import org.frikadelki.deepv.common.LightsSnippet
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.program.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Pd00Program(pipeline: Pipeline) {
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

    fun setVertexPosition(buffer: FloatBuffer, components: Vector4Components) {
        if (components.greaterThan(Vector4Components.THREE)) {
            throw ProgramException("Can't send that much components for this attribute.")
        }
        vertexPosition.enable()
        vertexPosition.setData(buffer, components, VertexAttributeHandle.ComponentType.FLOAT)
    }

    fun setVertexNormals(buffer: FloatBuffer, components: Vector4Components) {
        if (components.greaterThan(Vector4Components.THREE)) {
            throw ProgramException("Can't send that much components for this attribute.")
        }
        vertexNormal.enable()
        vertexNormal.setData(buffer, components, VertexAttributeHandle.ComponentType.FLOAT)
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