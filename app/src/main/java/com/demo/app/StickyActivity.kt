package com.demo.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.demo.app.adapter.VerticalAdapter

class StickyActivity : AppCompatActivity() {

    val rv1 by lazy { findViewById<RecyclerView>(R.id.rv1) }
    val rv2 by lazy { findViewById<RecyclerView>(R.id.rv2) }
//    val rv3 by lazy { findViewById<RecyclerView>(R.id.rv3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticky)


        rv1.adapter = VerticalAdapter(30)
        rv2.adapter = VerticalAdapter(30)
//        rv3.adapter = VerticalAdapter(60)

    }
    
    
    fun onClick(view: View) {
        Toast.makeText(this, "permanent click", Toast.LENGTH_SHORT).show()
    }
}