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

fun directFloatBuffer(floatsCapacity: Int): FloatBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(floatsCapacity * BYTES_PER_FLOAT)
    byteBuffer.order(ByteOrder.nativeOrder())
    return byteBuffer.asFloatBuffer()
}

fun directFloatBufferFromArray(input: FloatArray): FloatBuffer {
    val output = directFloatBuffer(input.size)
    output.put(input)
    output.position(0)
    return output
}

const val BYTES_PER_SHORT: Int = 2

fun directShortBuffer(shortsCapacity: Int): ShortBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(shortsCapacity * BYTES_PER_SHORT)
    byteBuffer.order(ByteOrder.nativeOrder())
    return byteBuffer.asShortBuffer()
}

fun directShortBufferFromArray(input: ShortArray): ShortBuffer {
    val output = directShortBuffer(input.size)
    output.put(input)
    output.position(0)
    return output
}
