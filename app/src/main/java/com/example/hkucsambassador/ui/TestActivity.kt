package com.example.hkucsambassador.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hkucsambassador.R
import com.example.hkucsambassador.api.Api
import com.example.hkucsambassador.data.Message
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import android.provider.Settings.Secure
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.security.AccessController.getContext

class TestActivity : AppCompatActivity()  {
    private lateinit var adapter: MessageAdapter
    /*private var output: String? = null
    private var recorder: MediaRecorder? = null
    private var recordState: Boolean = false*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        recyclerview_messages.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(this, recyclerview_messages)
        recyclerview_messages.adapter = adapter

        getApi("altitude_test")

        clickOnSubmit()
    }

    private fun clickOnSubmit() {
        submitButton.setOnClickListener {
            if (inputBar.text.isNotEmpty()) {
                val str = inputBar.text.toString()
                val m = Message(inputBar.text.toString(), "user", "", "")
                adapter.insertMessage(m)
                recyclerview_messages.scrollToPosition(adapter.itemCount - 1)
                inputBar.text.clear()
                getApi(str)
            }
        }
    }

    private fun getApi(str: String) {

        val api = Retrofit.Builder()
                .baseUrl("http://cs-chatbot.eastasia.cloudapp.azure.com:5001")
                .build()
                .create(Api::class.java)

        val deviceID = Secure.getString(getContentResolver(), Secure.ANDROID_ID)
        //Log.d("TAG", "ID:" + deviceID)
        val jsonObject = JSONObject()
        if (deviceID == null) {
            jsonObject.put("session_id", "tester")
        }
        else{
            jsonObject.put("session_id", deviceID)
        }
        jsonObject.put("text", str)

        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        Log.d("TAG", jsonObjectString)

        CoroutineScope(Dispatchers.IO).launch {
            val response = api.getMessage(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    val responseString = response.body()?.string()
                    val result = JSONObject(responseString)
                    val resultMessage = result.getJSONArray("messages")
                    val messageNum = resultMessage.length()

                    for (i in 0 until messageNum) {
                        var messageObj = resultMessage.getJSONObject(i)
                        when (messageObj.getString("type")) {
                            "text" -> {
                                Log.d("TAG", "text")
                                adapter.insertMessage(Message(messageObj.getString("text"), "bot", "", ""))
                                recyclerview_messages.scrollToPosition(adapter.itemCount - 1)
                            }
                            "quick_replies" -> {
                                Log.d("TAG", "quick_replies")
                                val m = Message(messageObj.getString("text"), "bot", "", "")
                                adapter.insertMessage(m)

                                var buttons = messageObj.getJSONArray("buttons")
                                for (b in 0 until buttons.length()) {
                                    var m = Message(
                                            buttons.getJSONObject(b).getString("title"),
                                            "button",
                                            buttons.getJSONObject(b).getString("type"),
                                            buttons.getJSONObject(b).getString("value"))
                                    adapter.insertMessage(m)
                                }
                                recyclerview_messages.scrollToPosition(adapter.itemCount - 1)
                            }
                            else -> {
                                Log.d("TAG", "cards")
                                var cards = messageObj.getJSONArray("cards")
                                var count = cards.length()
                                for (c in 0 until cards.length()){
                                    var m = Message(cards.getJSONObject(c).getString("title"), "cardInfo", cards.getJSONObject(c).getString("subtitle"), cards.getJSONObject(c).getString("image_url"))
                                    adapter.insertMessage(m)

                                    var cardButtons = cards.getJSONObject(c).getJSONArray("buttons")
                                    count += cardButtons.length()
                                    for (b in 0 until cardButtons.length()){
                                        var m = Message(cardButtons.getJSONObject(b).getString("title"), "cardButton", cardButtons.getJSONObject(b).getString("type"), cardButtons.getJSONObject(b).getString("value"))
                                        adapter.insertMessage(m)
                                    }
                                }
                                (recyclerview_messages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(adapter.itemCount-1-count, 0)

                            }
                        }
                    }

                } else {

                    Log.e("Error", response.code().toString())

                }
            }
        }
    }
}