/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/2
 */

package org.frikadelki.deepv.main

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet


class SurfaceView : GLSurfaceView {
    init {
        setEGLContextClientVersion(2)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
}