package com.xannanov.course

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xannanov.course.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivNumber.setValue(777)
    }
}