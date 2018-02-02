/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/2
 */

package org.frikadelki.deepv.render

import android.content.Context
import android.view.View


class MainGL(context: Context) {
    private val view: MainGLSurfaceView = MainGLSurfaceView(context)
    private val renderer: MainGLSurfaceRenderer = MainGLSurfaceRenderer()

    init {
        view.setRenderer(renderer)
    }

    val contentView: View get() = view
}