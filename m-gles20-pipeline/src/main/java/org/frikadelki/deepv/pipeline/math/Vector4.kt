/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/6
 */

package org.frikadelki.deepv.pipeline.math

import org.frikadelki.deepv.pipeline.directFloatBuffer
import java.nio.FloatBuffer

const val VECTOR4_SIZE = 4
internal const val V4_X = 0
internal const val V4_Y = 1
internal const val V4_Z = 2
internal const val V4_W = 3

private const val C0 = World.C0
private const val C1 = World.C1
private const val V4_POINT_W = C1

enum class Vector4Components(val count: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    ;

    fun greaterThan(components: Vector4Components): Boolean {
        return count > components.count
    }
}

class Vector4(private val data: FloatArray = FloatArray(VECTOR4_SIZE),
              private var offset: Int = 0) {
    init {
        if (offset < 0 || offset + VECTOR4_SIZE > data.size) {
            throw IllegalArgumentException("Bad offset and/or data array size.")
        }
    }

    constructor(x: Float = C0, y: Float = C0, z: Float = C0, w: Float = C0)
            : this(floatArrayOf(x, y, z, w), 0)

    fun length(): Float {
        return Math.sqrt((x*x + y*y + z*z + w*w).toDouble()).toFloat()
    }

    fun set(x: Float = this.x, y: Float = this.y, z: Float = this.z, w: Float = this.w) : Vector4 {
        data[offset + V4_X] = x
        data[offset + V4_Y] = y
        data[offset + V4_Z] = z
        data[offset + V4_W] = w
        return this
    }

    fun set(vector: Vector4): Vector4 {
        System.arraycopy(vector.data, vector.offset, data, offset, VECTOR4_SIZE)
        return this
    }

    fun point(x: Float = this.x, y: Float = this.y, z: Float = this.z) : Vector4 {
        set(x, y, z, V4_POINT_W)
        return this
    }

    fun translate(dx: Float = C0, dy: Float = C0, dz: Float = C0, dw: Float = C0) : Vector4 {
        data[offset + V4_X] += dx
        data[offset + V4_Y] += dy
        data[offset + V4_Z] += dz
        data[offset + V4_W] += dw
        return this
    }

    fun add(vector: Vector4): Vector4 {
        for(i in 0 until VECTOR4_SIZE) {
            data[offset + i] += vector.data[vector.offset + i]
        }
        return this
    }

    fun cross(v2: Vector4, out: Vector4 = Vector4()): Vector4 {
        val v1 = this
        out.set(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x,
                0.0f)
        return out
    }

    fun scale(scalar: Float): Vector4 {
        set(x*scalar, y*scalar, z*scalar, w*scalar)
        return this
    }

    fun negate(): Vector4 {
        scale(-1.0f)
        return this
    }

    fun pointWDivide(): Vector4 {
        if (isEpsilonZero(w)) {
            return this
        }
        set(x/w, y/w, z/w, V4_POINT_W)
        return this
    }

    fun normalize(): Vector4 {
        val length = length()
        set(x/length, y/length, z/length, w/length)
        return this
    }

    var x: Float
        get() = data[offset + V4_X]
        set(value) { data[offset + V4_X] = value }

    var y: Float
        get() = data[offset + V4_Y]
        set(value) { data[offset + V4_Y] = value }

    var z: Float
        get() = data[offset + V4_Z]
        set(value) { data[offset + V4_Z] = value }

    var w: Float
        get() = data[offset + V4_W]
        set(value) { data[offset + V4_W] = value }

    var r: Float
        get() = data[offset + V4_X]
        set(value) { data[offset + V4_X] = value }

    var g: Float
        get() = data[offset + V4_Y]
        set(value) { data[offset + V4_Y] = value }

    var b: Float
        get() = data[offset + V4_Z]
        set(value) { data[offset + V4_Z] = value }

    var a: Float
        get() = data[offset + V4_W]
        set(value) { data[offset + V4_W] = value }

    fun writeTo(out: FloatBuffer, components: Vector4Components) {
        if (components.count > out.remaining()) {
            throw IllegalArgumentException("out")
        }
        iterateComponents(components.count) { value, _ ->
            out.put(value)
        }
    }

    fun writeTo(out: FloatArray, outOffset: Int, components: Vector4Components = Vector4Components.FOUR) {
        if (outOffset + components.count > out.size) {
            throw IllegalArgumentException("buffer")
        }
        iterateComponents(components.count) { value, index ->
            out[outOffset + index] = value
        }
    }

    private inline fun iterateComponents(componentsCount: Int, iterator: (value: Float, index: Int) -> Unit) {
        if (componentsCount < 0 || componentsCount > VECTOR4_SIZE) {
            throw IllegalArgumentException("componentsCount")
        }
        for (i in 0 until componentsCount) {
            iterator(data[offset + i], i)
        }
    }

    fun accessAsArray(accessor: (data: FloatArray, offset: Int) -> Unit) {
        accessor(rawData, rawOffset)
    }

    //
    // used for some dirty trickery
    // like using a single instance of this class
    // to "slide" along continues float array and
    // treat separate regions as parts of different vectors
    // basically a reinterpret cast of sorts

    internal val rawData: FloatArray
        get() = data

    internal var rawOffset: Int
        get() = offset
        set(value) { offset = value }
}

class Vector4Array internal constructor(private val data: FloatArray,
                                        private val dataOffset: Int,
                                        val vectorsCount: Int,
                                        private val tmpVector: Vector4 = Vector4()) {
    init {
        if (dataOffset < 0) {
            throw IllegalArgumentException("dataOffset")
        }
        if (vectorsCount <= 0) {
            throw IllegalArgumentException("vectorsCount")
        }
        if (dataOffset + vectorsCount * VECTOR4_SIZE > data.size) {
            throw IllegalArgumentException("data|dataOffset|vectorsCount")
        }
    }

    constructor(vectorsCount: Int) : this(FloatArray(vectorsCount * VECTOR4_SIZE), 0, vectorsCount)

    private val access: Vector4 = Vector4(data, dataOffset)
    private var position: Int = 0

    private fun advancePosition() {
        position++
        access.rawOffset += VECTOR4_SIZE
    }

    fun rewind() {
        position = 0
        access.rawOffset = dataOffset
    }

    fun remaining(): Int {
        return vectorsCount - position
    }

    fun hasRemaining(): Boolean {
        return remaining() > 0
    }

    private fun checkRemaining() {
        if (!hasRemaining()) {
            throw IllegalStateException()
        }
    }

    fun replaceVector(visitor: (vector: Vector4) -> Unit) {
        checkRemaining()
        visitor(access)
        advancePosition()
    }

    fun readVector(out: Vector4) {
        checkRemaining()
        out.set(access)
        advancePosition()
    }

    fun readVector(out: Vector4Array) {
        checkRemaining()
        out.putVector(access)
        advancePosition()
    }

    fun putVector(vector: Vector4) {
        checkRemaining()
        access.set(vector)
        advancePosition()
    }

    fun putVector(x: Float, y: Float, z: Float, w: Float) {
        checkRemaining()
        access.set(x, y, z, w)
        advancePosition()
    }

    fun putPoint(x: Float, y: Float, z: Float) {
        checkRemaining()
        access.set(x, y, z, V4_POINT_W)
        advancePosition()
    }

    fun multiplyAll(matrix: Matrix4) {
        rewind()
        for(i in 0 until vectorsCount) {
            access.set(matrix.multiply(access, tmpVector))
            advancePosition()
        }
    }

    fun perspectiveDivideAll() {
        rewind()
        for(i in 0 until vectorsCount) {
            access.pointWDivide()
            advancePosition()
        }
    }

    fun normalizeAll() {
        rewind()
        for(i in 0 until vectorsCount) {
            access.normalize()
            advancePosition()
        }
    }

    operator fun get(index: Int): Vector4 {
        return Vector4(data, dataOffset + index * VECTOR4_SIZE)
    }

    fun slice(vectorsOffset: Int, sliceVectorsCount: Int): Vector4Array {
        if (vectorsOffset < 0) {
            throw IllegalArgumentException("vectorOffset")
        }
        if (sliceVectorsCount <= 0) {
            throw IllegalArgumentException("sliceVectorsCount")
        }
        val dataOffsetBoundary = dataOffset + (vectorsCount - 1) * VECTOR4_SIZE
        val sliceStartDataOffset = dataOffset + vectorsOffset * VECTOR4_SIZE
        if (sliceStartDataOffset > dataOffsetBoundary) {
            throw IllegalArgumentException("vectorOffset is out of bounds")
        }
        if (vectorsOffset + sliceVectorsCount > vectorsCount) {
            throw IllegalArgumentException("vectorOffset + sliceVectorsCount is out of bounds")
        }
        return Vector4Array(data, sliceStartDataOffset, sliceVectorsCount, tmpVector)
    }

    fun toDirectFloatBuffer(components: Vector4Components) : FloatBuffer {
        val output = directFloatBuffer(components.count * vectorsCount)
        rewind()
        for (i in 0 until vectorsCount) {
            access.writeTo(output, components)
            advancePosition()
        }
        output.rewind()
        return output
    }

    internal val rawData: FloatArray
        get() = data

    internal val rawOffset: Int
        get() = dataOffset
}