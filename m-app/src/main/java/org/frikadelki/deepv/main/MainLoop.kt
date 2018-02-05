/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.main

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import org.frikadelki.deepv.pipeline.Pipeline
import org.frikadelki.deepv.scene.SimplestDrawCall
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainLoop : GLSurfaceView.Renderer {
    private var pipeline: Pipeline? = null
    private var drawCall: SimplestDrawCall? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        pipeline = Pipeline()
        drawCall = SimplestDrawCall(pipeline!!)

        GLES20.glClearColor(0.5f, 0.0f, 0.0f, 1.0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        drawCall?.onViewportChange(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawCall?.draw()
    }
}



