/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/14
 */

package org.frikadelki.deepv.main

import org.frikadelki.deepv.pipeline.Pipeline


interface SceneLoop {
    fun onPipelineCreated(pipeline: Pipeline)
    fun onPipelineDisposed()

    fun onSurfaceChanged(width: Int, height: Int)

    fun onUpdateAnimations(deltaMillis: Long)
    fun onDrawFrame()
}