/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common

import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.frikadelki.deepv.pipeline.program.Program
import org.frikadelki.deepv.pipeline.program.UniformHandle


class LightsSnippet(private val maxSimpleLights: Int) {
    init {
        if (maxSimpleLights <= 0) {
            throw IllegalArgumentException()
        }
    }

    val fragmentSource: String =
            """
                uniform vec4 lightsAmbient;

                // w component determines type of light
                // -1 - directed light, xyz determine source direction
                //  0 - this light is off
                // +1 - point light, xyz determine source position
                uniform vec4 lightsSimpleSpec[$maxSimpleLights];

                uniform vec4 lightsSimpleColor[$maxSimpleLights];

                struct LightsIntensity {
                    vec4 ambient;
                    vec4 diffuse;
                    vec4 specular;
                };

                LightsIntensity lightsIntensity(vec3 position, vec3 normal, vec3 eye, float shininess) {
                    LightsIntensity light = LightsIntensity(lightsAmbient, vec4(0.0), vec4(0.0));
                    for(int i = 0; i < $maxSimpleLights; i++) {
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
            """.trimIndent()

    fun makeBinding(program: Program): LightsBinding {
        return LightsBinding(program, maxSimpleLights)
    }
}

class LightsBinding(program: Program, maxSimpleLights: Int) {
    init {
        if (maxSimpleLights <= 0) {
            throw IllegalArgumentException()
        }
    }
    private val lightsExporterTmp = LightsExporter(maxSimpleLights)

    private val lightsAmbientColor: UniformHandle = program.uniform("lightsAmbient")
    private val lightsSimpleSpec: UniformHandle = program.uniform("lightsSimpleSpec")
    private val lightsSimpleColor: UniformHandle = program.uniform("lightsSimpleColor")

    fun setLights(lights: Lights) {
        lightsExporterTmp.rebuild(lights.directLights, lights.pointLights)

        lightsAmbientColor.setVector(lights.ambient)
        lightsSimpleSpec.setVectorArray(lightsExporterTmp.simpleLightsSpecs)
        lightsSimpleColor.setVectorArray(lightsExporterTmp.simpleLightsColors)
    }
}

private const val SIMPLE_LIGHTS_SPEC_W_DIRECT = -1.0f
private const val SIMPLE_LIGHTS_SPEC_W_OFF = 0.0f
private const val SIMPLE_LIGHTS_SPEC_W_POINT = 1.0f

internal class LightsExporter(maxSimpleLights: Int) {
    init {
        if (maxSimpleLights <= 0) {
            throw IllegalArgumentException()
        }
    }

    val simpleLightsSpecs = Vector4Array(maxSimpleLights)
    val simpleLightsColors = Vector4Array(maxSimpleLights)

    init {
        rebuild(emptyList(), emptyList())
    }

    fun rebuild(directs: List<Lights.Direct>, points: List<Lights.Point>) {
        simpleLightsSpecs.rewind()
        simpleLightsColors.rewind()
        directs.forEach {
            val direction = it.direction
            simpleLightsSpecs.putVector(direction.x, direction.y, direction.z, SIMPLE_LIGHTS_SPEC_W_DIRECT)
            simpleLightsColors.putVector(it.color)
        }
        points.forEach {
            val origin = it.origin
            simpleLightsSpecs.putVector(origin.x, origin.y, origin.z, SIMPLE_LIGHTS_SPEC_W_POINT)
            simpleLightsColors.putVector(it.color)
        }
        while (simpleLightsSpecs.hasRemaining()) {
            simpleLightsSpecs.putVector(0.0f, 0.0f, 0.0f, SIMPLE_LIGHTS_SPEC_W_OFF)
            simpleLightsColors.putVector(0.0f, 0.0f, 0.0f, 0.0f)
        }
    }
}