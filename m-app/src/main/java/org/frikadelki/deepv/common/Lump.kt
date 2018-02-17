/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by ein on 2018/2/17
 */

package org.frikadelki.deepv.common

interface Lump {
    fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext)
    fun onUpdateAnimations(deltaMillis: Long)
}

abstract class EmptyLump : Lump {
    override fun onDraw(pawn: Pawn, scene: Scene, context: Scene.DrawContext) {
    }

    override fun onUpdateAnimations(deltaMillis: Long) {
    }
}