package org.frikadelki.deepv

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import org.frikadelki.deepv.render.MainGL

class MainActivity : AppCompatActivity() {
    private lateinit var mainGL: MainGL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainGL = MainGL(this)
        val contentFrame = findViewById<FrameLayout>(R.id.screen_main_content_frame)
        val contentLayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        contentFrame.addView(mainGL.contentView, contentLayoutParams)
    }
}
