package com.example.hkucsambassador.utils

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.example.hkucsambassador.data.Message

object BotMessageSimple {

    fun getBotMessageSimple (message: Message): String {

        return if (message.message.contains("hello")) {
            "Hello, I am HKUCS Ambassador. How may I help you?"
        }
        else if (message.message.contains("score")) {
            "The admission score (Best 5) of BEng Programme in 2020 is:" + "\n" +
                    " 26 (Upper Quartile)" + "\n" + " 25 (Median)" + "\n" + " 23 (Lower Quartile)"
        }
        else {
            "Please visit the homepage of the Department of Computer Science."
        }
    }

}