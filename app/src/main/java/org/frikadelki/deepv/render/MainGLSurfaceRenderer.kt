/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/2
 */

package org.frikadelki.deepv.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainGLSurfaceRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(unused: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.0f, 0.0f, 1.0f)
    }

    override fun onSurfaceChanged(unused: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(unused: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }
}