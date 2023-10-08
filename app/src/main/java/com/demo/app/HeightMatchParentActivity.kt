package com.demo.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.demo.app.adapter.VerticalAdapter

class HeightMatchParentActivity : AppCompatActivity() {

    val rv1 by lazy { findViewById<RecyclerView>(R.id.rv1) }
    val rv2 by lazy { findViewById<RecyclerView>(R.id.rv2) }
    val rv3 by lazy { findViewById<RecyclerView>(R.id.rv3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_height_match_parent)


        rv1.adapter = VerticalAdapter(20)
        rv2.adapter = VerticalAdapter(20)
        rv3.adapter = VerticalAdapter(20)

    }
    
    
    fun onClick(view: View) {
        Toast.makeText(this, "permanent click", Toast.LENGTH_SHORT).show()
    }
}