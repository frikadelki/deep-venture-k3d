/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/27
 */

package org.frikadelki.kash

import org.frikadelki.kash.klang.dirtyProperty
import org.junit.Assert
import org.junit.Assume
import org.junit.Test


class PropertiesTests {
    private class DirtyPropertyClient(init: Int) {
        private var dataSource = init

        private val property = dirtyProperty { dataSource }

        val value by property

        fun setData(value: Int) {
            dataSource = value
        }

        fun setDataReady() {
            property.markDirty()
        }
    }

    @Test
    fun dirtyPropertyBuildsFirstValue() {
        val value = 5

        val client = DirtyPropertyClient(value)
        Assert.assertEquals(value, client.value)
    }

    @Test
    fun dirtyPropertyCachesValue() {
        val valueOne = 5
        val valueTwo = 298

        val client = DirtyPropertyClient(valueOne)
        val fetchedValueOne = client.value
        Assume.assumeTrue( fetchedValueOne == valueOne)

        client.setData(valueTwo)
        Assert.assertEquals(client.value, valueOne)
    }

    @Test
    fun dirtyPropertyRefreshes() {
        val valueOne = 5
        val valueTwo = 298

        val client = DirtyPropertyClient(valueOne)
        val fetchedValueOne = client.value
        Assume.assumeTrue( fetchedValueOne == valueOne)

        client.setData(valueTwo)
        client.setDataReady()
        Assert.assertEquals(client.value, valueTwo)
    }
}