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
private const val V4_VECTOR_W = C0
private const val V4_POINT_W = C1

enum class Vector4Components(val count: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    ;

    fun greaterThan(components: Vector4Components): Boolean {
        return count > components.count
    }
}

interface Vector4RO {
    val x: Float
    val y: Float
    val z: Float
    val w: Float
}

class Vector4(private val data: FloatArray = FloatArray(VECTOR4_SIZE),
              private var offset: Int = 0)
    : Vector4RO {
    init {
        if (offset < 0 || offset + VECTOR4_SIZE > data.size) {
            throw IllegalArgumentException("Bad offset and/or data array size.")
        }
    }

    constructor(x: Float = C0, y: Float = C0, z: Float = C0, w: Float = C0)
            : this(floatArrayOf(x, y, z, w), 0)

    fun setPoint(x: Float = this.x, y: Float = this.y, z: Float = this.z): Vector4 {
        set(x, y, z, V4_POINT_W)
        return this
    }

    fun setVector(x: Float = this.x, y: Float = this.y, z: Float = this.z): Vector4 {
        set(x, y, z, V4_VECTOR_W)
        return this
    }

    fun set(x: Float = this.x, y: Float = this.y, z: Float = this.z, w: Float = this.w): Vector4 {
        data[offset + V4_X] = x
        data[offset + V4_Y] = y
        data[offset + V4_Z] = z
        data[offset + V4_W] = w
        return this
    }

    fun set(vector: Vector4RO): Vector4 {
        return if (vector is Vector4) {
            set(vector)
        } else {
            set(vector.x, vector.y, vector.z, vector.w)
        }
    }

    fun set(vector: Vector4): Vector4 {
        System.arraycopy(vector.data, vector.offset, data, offset, VECTOR4_SIZE)
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

    fun scale(scalar: Float): Vector4 {
        set(x*scalar, y*scalar, z*scalar, w*scalar)
        return this
    }

    fun negate(): Vector4 {
        scale(-1.0f)
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

    fun pointWDivide(): Vector4 {
        if (w.isEpsilonZero()) {
            throw IllegalStateException("Not a point.")
            // return this
        }
        set(x/w, y/w, z/w, V4_POINT_W)
        return this
    }

    fun vectorNormalize(): Vector4 {
        if (!w.isEpsilonZero()) {
            throw IllegalStateException("Not a vector.")
        }
        val length = length()
        setVector(x/length, y/length, z/length)
        return this
    }

    fun length(): Float {
        return Math.sqrt((x*x + y*y + z*z + w*w).toDouble()).toFloat()
    }

    override var x: Float
        get() = data[offset + V4_X]
        set(value) { data[offset + V4_X] = value }

    override var y: Float
        get() = data[offset + V4_Y]
        set(value) { data[offset + V4_Y] = value }

    override var z: Float
        get() = data[offset + V4_Z]
        set(value) { data[offset + V4_Z] = value }

    override var w: Float
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
                                        val vectorsCount: Int) {
    private val floatsCount = vectorsCount * VECTOR4_SIZE

    init {
        if (dataOffset < 0) {
            throw IllegalArgumentException("dataOffset")
        }
        if (vectorsCount <= 0) {
            throw IllegalArgumentException("vectorsCount")
        }
        if (dataOffset + floatsCount > data.size) {
            throw IllegalArgumentException("data|dataOffset|vectorsCount")
        }
    }

    constructor(vectorsCount: Int) : this(FloatArray(vectorsCount * VECTOR4_SIZE), 0, vectorsCount)

    private val tmpVector: Vector4 = Vector4()

    private val access: Vector4 = Vector4(data, dataOffset)
    private var position: Int = 0

    private fun advancePosition(count: Int = 1) {
        position += count
        access.rawOffset += count * VECTOR4_SIZE
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

    fun putVector(vector: Vector4): Vector4 {
        checkRemaining()
        access.set(vector)
        advancePosition()
        return vector
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

    fun forEachRemaining(visitor: (vector: Vector4) -> Unit) {
        while (hasRemaining()) {
            visitor(access)
            advancePosition()
        }
    }

    fun writeRemainingTo(output: Vector4Array, visitor: ((vector: Vector4) -> Unit)? = null) {
        if (!hasRemaining()) {
            return
        }
        if (remaining() > output.remaining()) {
            throw IllegalArgumentException()
        }
        val writeVectorsCount = remaining()
        System.arraycopy(
                access.rawData, access.rawOffset,
                output.access.rawData, output.access.rawOffset,
                writeVectorsCount * VECTOR4_SIZE)
        advancePosition(writeVectorsCount)
        if (visitor != null) {
            for (i in 0 until writeVectorsCount) {
                visitor(output.access)
                output.advancePosition()
            }
        } else {
            output.advancePosition(writeVectorsCount)
        }
    }

    fun nextSlice(): Vector4 {
        checkRemaining()
        val slice = slice(position)
        advancePosition()
        return slice
    }

    // specific group operations that apply to all elements and reset position to the end

    fun multiplyAll(matrix: Matrix4) {
        rewind()
        for (i in 0 until vectorsCount) {
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
            access.vectorNormalize()
            advancePosition()
        }
    }

    // slicing/direct access/export operations

    fun slice(vectorsOffset: Int): Vector4 {
        return Vector4(data, dataOffset + vectorsOffset * VECTOR4_SIZE)
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
        return Vector4Array(data, sliceStartDataOffset, sliceVectorsCount)
    }

    fun copy(): Vector4Array {
        val copy = Vector4Array(vectorsCount)
        System.arraycopy(data, dataOffset, copy.data, 0, floatsCount)
        return copy
    }

    fun toDirectFloatBuffer(components: Vector4Components): FloatBuffer {
        val output = directFloatBuffer(components.count * vectorsCount)
        var dataOffset = this.dataOffset
        for (i in 0 until vectorsCount) {
            output.put(data, dataOffset, components.count)
            dataOffset += VECTOR4_SIZE
        }
        output.rewind()
        return output
    }

    internal val rawData: FloatArray
        get() = data

    internal val rawOffset: Int
        get() = dataOffset
}