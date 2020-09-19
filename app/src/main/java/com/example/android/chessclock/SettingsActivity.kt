package com.example.android.chessclock

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var myPagerAdapter : MyFragmentPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        myPagerAdapter = MyFragmentPagerAdapter(supportFragmentManager)
        viewPager.adapter = myPagerAdapter
    }
}