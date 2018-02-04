/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.pipeline

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


const val BYTES_PER_FLOAT: Int = 4

fun directFloatBufferFromArray(input: FloatArray): FloatBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(input.size * BYTES_PER_FLOAT)
    byteBuffer.order(ByteOrder.nativeOrder())

    val output = byteBuffer.asFloatBuffer()
    output.put(input)
    output.position(0)

    return output
}

const val BYTES_PER_SHORT: Int = 2

fun directShortBufferFormArray(input: ShortArray): ShortBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(input.size * BYTES_PER_SHORT)
    byteBuffer.order(ByteOrder.nativeOrder())

    val output = byteBuffer.asShortBuffer()
    output.put(input)
    output.position(0)

    return output
}
