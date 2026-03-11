package com.example.softpos_demo

import android.app.Application
import com.cardtek.softpos.utils.ProcessCheckUtil

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Recommended by guide: skip normal app init in SDK special processes
        if (ProcessCheckUtil.isSpecialProcess(this)) {
            return
        }
    }
}