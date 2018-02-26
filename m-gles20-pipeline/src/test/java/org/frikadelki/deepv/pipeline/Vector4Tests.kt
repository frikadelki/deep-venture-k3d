/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/12
 */

package org.frikadelki.deepv.pipeline

import org.frikadelki.deepv.pipeline.math.VECTOR4_SIZE
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Components
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.FloatBuffer
import java.util.*

class Vector4Tests {
    private val testVectorArray = floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f)

    @Test
    fun writeToBuffer() {
        val testVector = Vector4(testVectorArray)
        val outputBuffer = FloatBuffer.allocate(VECTOR4_SIZE)
        testVector.writeTo(outputBuffer, Vector4Components.FOUR)
        outputBuffer.position(0)

        val contents = FloatArray(VECTOR4_SIZE)
        outputBuffer.get(contents)

        assertTrue(Arrays.equals(testVectorArray, contents))
    }

    @Test
    fun writeToBufferPart() {
        val testComponentsCount = Vector4Components.TWO

        val testVector = Vector4(testVectorArray)
        val outputBuffer = FloatBuffer.allocate(testComponentsCount.count)
        testVector.writeTo(outputBuffer, testComponentsCount)
        outputBuffer.position(0)

        val contents = FloatArray(testComponentsCount.count)
        outputBuffer.get(contents)

        for (i in 0 until testComponentsCount.count) {
            assertEquals(testVectorArray[i], contents[i], 0.0f)
        }
    }

    @Test
    fun writeToArray() {
        val testVector = Vector4(testVectorArray)
        val outputArray = FloatArray(VECTOR4_SIZE)
        testVector.writeTo(outputArray, 0)

        assertTrue(Arrays.equals(testVectorArray, outputArray))
    }

    @Test
    fun writeToArrayWithOffset() {
        val outOffset = 2

        val testVector = Vector4(testVectorArray)
        val outputArray = FloatArray(outOffset + 1 + VECTOR4_SIZE)
        testVector.writeTo(outputArray, outOffset)

        for (i in 0 until VECTOR4_SIZE) {
            assertEquals(testVectorArray[i], outputArray[outOffset + i], 0.0f)
        }
    }

    @Test
    fun writeToArrayWithOffsetPart() {
        val outOffset = 2
        val components = Vector4Components.THREE

        val testVector = Vector4(testVectorArray)
        val outputArray = FloatArray(outOffset + 1 + components.count)
        testVector.writeTo(outputArray, outOffset, components)

        for (i in 0 until components.count) {
            assertEquals(testVectorArray[i], outputArray[outOffset + i], 0.0f)
        }
    }
}