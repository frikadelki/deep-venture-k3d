/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.common

class DrawContext {
}

interface Lump {
    fun onDraw(pawn: Pawn, scene: Scene, context: DrawContext)
}

class Pawn {
    private val lumps = mutableListOf<Lump>()
    private val owned = mutableListOf<Pawn>()

    val transform = Transform()

    fun addLump(lump: Lump) {
        lumps.add(lump)
    }

    fun addOwnedPawn(pawn: Pawn) {
        owned.add(pawn)
    }

    fun onDraw(scene: Scene, context: DrawContext) {
        //TODO: implement transforms matrix stack
        lumps.forEach { it.onDraw(this, scene, context) }
        owned.forEach { it.onDraw(scene, context) }
    }
}

class Scene {
    private val rootPawn = Pawn()

    val camera = Camera()

    fun onDraw() {
        rootPawn.onDraw(this, DrawContext())
    }

    fun addPawn(pawn: Pawn) {
        rootPawn.addOwnedPawn(pawn)
    }
}

