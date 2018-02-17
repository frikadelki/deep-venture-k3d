/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.common

class Scene {
    class DrawContext {
    }

    private val rootPawn = Pawn()

    val camera = Camera()

    val lights = Lights()

    fun onDraw() {
        rootPawn.onDraw(this, DrawContext())
    }

    fun onUpdateAnimations(deltaMillis: Long) {
        rootPawn.onUpdateAnimations(deltaMillis)
    }

    fun addPawn(pawn: Pawn) {
        rootPawn.addOwnedPawn(pawn)
    }
}