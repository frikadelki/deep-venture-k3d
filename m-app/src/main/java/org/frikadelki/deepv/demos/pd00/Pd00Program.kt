/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.demos.pd00

import org.frikadelki.deepv.common.Lights
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.pipeline.math.Matrix4
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.frikadelki.deepv.pipeline.program.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Pd00Program(pipeline: Pipeline) {
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

                uniform vec4 lightsAmbient;

                // w component determines type of light
                // -1 - directed light, xyz determine source direction
                //  0 - this light is off
                // +1 - point light, xyz determine source position
                uniform vec4 lightsSimpleSpec[$SIMPLE_LIGHTS_MAX];

                uniform vec4 lightsSimpleColor[$SIMPLE_LIGHTS_MAX];

                struct LightIntensity {
                    vec4 ambient;
                    vec4 diffuse;
                    vec4 specular;
                };

                LightIntensity calculateLight(vec3 position, vec3 normal, vec3 eye, float shininess) {
                    LightIntensity light = LightIntensity(lightsAmbient, vec4(0.0), vec4(0.0));
                    for(int i = 0; i < $SIMPLE_LIGHTS_MAX; i++) {
                        vec4 lightSpec = lightsSimpleSpec[i];
                        vec3 lightDirection;
                        if (lightSpec.w < 0.0) {
                            lightDirection = normalize(lightSpec.xyz);
                        } else if (lightSpec.w < 1.0) {
                            lightDirection = -normal;
                        } else {
                            lightDirection = normalize(lightSpec.xyz - position);
                        }
                        float lambertian = dot(lightDirection, normal);
                        if (lambertian <= 0.0) {
                            continue;
                        }
                        vec3 viewDirection = eye - position;
                        vec3 lightViewHalf = normalize(lightDirection + viewDirection);
                        float specularAngle = dot(lightViewHalf, normal);

                        vec4 lightColor = lightsSimpleColor[i];
                        light.diffuse += max(0.0, lambertian) * lightColor;
                        light.specular += pow(max(0.0, specularAngle), shininess) * lightColor;
                    }
                    return light;
                }

                uniform vec4 cameraEyePosition;

                uniform vec4 modelColorDiffuse;
                uniform vec4 modelColorSpecular;

                varying vec3 varPosition;
                varying vec3 varNormal;

                void main() {
                    LightIntensity light = calculateLight(varPosition, varNormal, cameraEyePosition.xyz, modelColorSpecular.w);
                    gl_FragColor = (light.ambient + light.diffuse) * modelColorDiffuse
                                + light.specular * modelColorSpecular;
                }

            """.trimIndent())

    private val program: Program = pipeline.loadProgram(programSource)

    private val viewProjectionMatrix: UniformHandle = program.uniform("viewProjectionMatrix")
    private val cameraEyePosition: UniformHandle = program.uniform("cameraEyePosition")

    private val lightsExporterTmp = Pd00LightsExporter()
    private val lightsAmbientColor: UniformHandle = program.uniform("lightsAmbient")
    private val lightsSimpleSpec: UniformHandle = program.uniform("lightsSimpleSpec")
    private val lightsSimpleColor: UniformHandle = program.uniform("lightsSimpleColor")

    private val modelMatrix: UniformHandle = program.uniform("modelMatrix")
    private val modelColorDiffuse: UniformHandle = program.uniform("modelColorDiffuse")
    private val modelColorSpecular: UniformHandle = program.uniform("modelColorSpecular")

    private val vertexPosition: VertexAttributeHandle = program.vertexAttribute("vPosition")
    private val vertexNormal: VertexAttributeHandle = program.vertexAttribute("vNormal")

    fun enable() {
        program.use()
    }

    fun setViewProjectionMatrix(matrix: Matrix4) {
        viewProjectionMatrix.setMatrix(matrix)
    }

    fun setCameraEyePosition(position: Vector4) {
        cameraEyePosition.setVector(position)
    }

    fun setLights(lights: Lights) {
        lightsExporterTmp.rebuild { builder ->
            lights.directLights.forEach { builder.addDirect(it.direction, it.color) }
            lights.pointLights.forEach { builder.addPoint(it.origin, it.color) }
        }

        lightsAmbientColor.setVector(lights.ambient)
        lightsSimpleSpec.setVectorArray(lightsExporterTmp.simpleLightsSpecs)
        lightsSimpleColor.setVectorArray(lightsExporterTmp.simpleLightsColors)
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

private const val SIMPLE_LIGHTS_MAX: Int = 2
private const val SIMPLE_LIGHTS_SPEC_W_DIRECT = -1.0f
private const val SIMPLE_LIGHTS_SPEC_W_OFF = 0.0f
private const val SIMPLE_LIGHTS_SPEC_W_POINT = 1.0f

private class Pd00LightsExporter {
    interface Builder {
        fun addDirect(lightDirection: Vector4, color: Vector4)
        fun addPoint(lightOrigin: Vector4, color: Vector4)
    }

    val simpleLightsSpecs = Vector4Array(SIMPLE_LIGHTS_MAX)
    val simpleLightsColors = Vector4Array(SIMPLE_LIGHTS_MAX)

    init {
        rebuild { }
    }

    fun rebuild(build: (builder: Builder) -> Unit) {
        simpleLightsSpecs.rewind()
        simpleLightsColors.rewind()
        build(object: Builder {
            override fun addDirect(lightDirection: Vector4, color: Vector4) {
                simpleLightsSpecs.putVector(lightDirection.x, lightDirection.y, lightDirection.z, SIMPLE_LIGHTS_SPEC_W_DIRECT)
                simpleLightsColors.putVector(color)
            }

            override fun addPoint(lightOrigin: Vector4, color: Vector4) {
                simpleLightsSpecs.putVector(lightOrigin.x, lightOrigin.y, lightOrigin.z, SIMPLE_LIGHTS_SPEC_W_POINT)
                simpleLightsColors.putVector(color)
            }
        })
        while (simpleLightsSpecs.hasRemaining()) {
            simpleLightsSpecs.putVector(0.0f, 0.0f, 0.0f, SIMPLE_LIGHTS_SPEC_W_OFF)
            simpleLightsColors.putVector(0.0f, 0.0f, 0.0f, 0.0f)
        }
    }
}