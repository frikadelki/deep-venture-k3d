/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.main

import android.opengl.GLSurfaceView
import org.frikadelki.deepv.pipeline.Pipeline
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainLoop(private val sceneLoop: SceneLoop) : GLSurfaceView.Renderer {
    private var pipeline: Pipeline? = null

    private val frameAnimationStepMillisThreshold: Long = 12
    private val frameAnimationDeltaMillisClampTop: Long = 100
    private var lastFrameMillis: Long = Long.MAX_VALUE

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        pipeline = Pipeline()
        sceneLoop.onPipelineCreated(pipeline!!)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        sceneLoop.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val currentFrameMillis = System.currentTimeMillis()
        val animateMillis: Long =
                if (lastFrameMillis < currentFrameMillis) {
                    val delta = currentFrameMillis - lastFrameMillis
                    if (delta > frameAnimationStepMillisThreshold) {
                        Math.min(delta, frameAnimationDeltaMillisClampTop)
                        delta
                    } else {
                        -1
                    }
                } else {
                    -1
                }
        lastFrameMillis = currentFrameMillis

        if (animateMillis > 0) {
            sceneLoop.onUpdateAnimations(animateMillis)

        }
        sceneLoop.onDrawFrame()
    }
}



