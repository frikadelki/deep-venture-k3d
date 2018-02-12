/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/12
 */

package pipeline

import org.frikadelki.deepv.pipeline.math.VECTOR4_SIZE
import org.frikadelki.deepv.pipeline.math.Vector4
import org.frikadelki.deepv.pipeline.math.Vector4Array
import org.junit.Assert
import org.junit.Test
import java.util.*

class Vector4ArraySliceTests {

    @Test
    fun putOneWithExactBacking() {
        val testVector = Vector4(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f))
        val backingArray = FloatArray(VECTOR4_SIZE)
        val slice = Vector4Array(backingArray, 0, 1)
        slice.put(testVector)

        Assert.assertTrue(Arrays.equals(backingArray, testVector.rawData))
    }

    @Test
    fun putOneWithLargeBacking() {
        val backingSize = VECTOR4_SIZE * 5
        val backingOffset = 3

        val testVector = Vector4(floatArrayOf(1.0f, 2.0f, 3.0f, 4.0f))
        val backingArray = FloatArray(backingSize)
        val slice = Vector4Array(backingArray, backingOffset, 1)
        slice.put(testVector)

        for (i in 0 until VECTOR4_SIZE) {
            Assert.assertEquals(testVector.rawData[i], backingArray[backingOffset + i])
        }
    }
}