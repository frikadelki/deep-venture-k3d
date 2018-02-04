/*
 * Deep Venture K3D
 * Copyright 2018 -*- frikadelki-corps -*-
 * Created by frikadelki on 2018/2/5
 */

package org.frikadelki.deepv.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import org.frikadelki.deepv.R

class MainActivity : AppCompatActivity() {
    private lateinit var mainLoop: MainLoop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val surfaceView = SurfaceView(this)
        val contentFrame = findViewById<FrameLayout>(R.id.screen_main_content_frame)
        val contentLayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        contentFrame.addView(surfaceView, contentLayoutParams)

        mainLoop = MainLoop()
        surfaceView.setRenderer(mainLoop)
    }
}
