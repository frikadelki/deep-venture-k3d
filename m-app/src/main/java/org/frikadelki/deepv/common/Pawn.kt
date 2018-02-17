/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common

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

    fun onDraw(scene: Scene, context: Scene.DrawContext) {
        //TODO: implement transforms matrix stack
        lumps.forEach { it.onDraw(this, scene, context) }
        owned.forEach { it.onDraw(scene, context) }
    }

    fun onUpdateAnimations(deltaMillis: Long) {
        lumps.forEach { it.onUpdateAnimations(deltaMillis) }
        owned.forEach { it.onUpdateAnimations(deltaMillis) }
    }
}