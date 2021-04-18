package com.example.hkucsambassador.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hkucsambassador.R
import com.example.hkucsambassador.api.Api
import com.example.hkucsambassador.data.Message
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.FileInputStream
import java.io.IOException


class MessageAdapter(val context: Context, val mLayoutManager: RecyclerView): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var listOfMessages = mutableListOf<Message>()

    open inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(message: Message){
        }

    }

    inner class UserMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var userMessageText: TextView = itemView.findViewById(R.id.userTextView)

        override fun bind(message: Message){
            userMessageText.text = message.message
        }
    }

    inner class BotMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var botMessageText: TextView = itemView.findViewById(R.id.chatbotTextView)

        override fun bind(message: Message){
            botMessageText.text = message.message.replace("<br>", "\r")
        }
    }

    inner class ButtonMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var botButton: Button = itemView.findViewById(R.id.chatbotButton)

        override fun bind(message: Message){
            botButton.text = message.message.replace("<br>", "\r")
            botButton.setOnClickListener {
                run {
                    when (message.type) {
                        "payload" -> {
                            val m = Message(message.message, "user", "", "")
                            insertMessage(m)
                            getApi(message.value)
                        }
                        "web_url" -> {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(message.value)))
                        }
                        else -> {
                            Log.d("invalid type", "")
                        }
                    }
                }
            }
        }
    }

    inner class CardInfoMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var img: ImageView = itemView.findViewById(R.id.image)
        private var title: TextView = itemView.findViewById(R.id.text)
        private var subtitle: TextView = itemView.findViewById(R.id.text2)

        override fun bind(message: Message) {
            title.text = message.message.replace("<br>", "\r")
            subtitle.text = message.type.replace("<br>", "\r")
            if(message.value != ""){
                Picasso.get().load(message.value).into(img)
            }
        }
    }

    inner class CardButtonMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var cardButton: Button = itemView.findViewById(R.id.button)

        override fun bind(message: Message){
            cardButton.text = message.message.replace("<br>", "\r")
            cardButton.setOnClickListener {
                run {
                    when (message.type) {
                        "payload" -> {
                            val m = Message(message.message, "user", "", "")
                            insertMessage(m)
                            getApi(message.value)
                        }
                        "web_url" -> {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(message.value)))
                        }
                        else -> {
                            Log.d("invalid type", "")
                        }
                    }
                }
            }
        }
    }

    inner class AudioMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private var audioButton: Button = itemView.findViewById(R.id.audioButton)

        override fun bind(message: Message){
            audioButton.setOnClickListener {
                run {
                    getTtsApi(message.message)
                }
            }
        }
    }

    fun insertMessage(message:Message){
        listOfMessages.add(message)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == 1) {
            UserMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.user_messages, parent, false)
            )
        }
        else if (viewType == 2) {
            BotMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.bot_messages, parent, false)
            )
        }
        else if (viewType == 3) {
            ButtonMessageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.bot_buttons, parent, false)
            )
        }
        else if (viewType == 4) {
            CardInfoMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.bot_card_info, parent, false)
            )
        }
        else if (viewType == 5) {
            CardButtonMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.bot_card_button, parent, false)
            )
        }
        else {
            AudioMessageViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.audio_button, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return listOfMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        val m = listOfMessages [position]

        return if (m.sender == "user") {
            1
        }
        else if (m.sender == "bot") {
            2
        }
        else if (m.sender == "button"){
            3
        }
        else if (m.sender == "cardInfo"){
            4
        }
        else if (m.sender == "cardButton"){
            5
        }
        else{
            6
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val m = listOfMessages[position]

        holder.bind(m)

    }

    private fun getApi(str: String) {

        val api = Retrofit.Builder()
                .baseUrl("http://cs-chatbot.eastasia.cloudapp.azure.com:5001")
                .build()
                .create(Api::class.java)

        val deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        Log.d("TAG", "ID:" + deviceID)
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
                                insertMessage(Message(messageObj.getString("text"), "bot", "", ""))
                                mLayoutManager.scrollToPosition(itemCount - 1)
                            }
                            "quick_replies" -> {
                                Log.d("TAG", "quick_replies")
                                val m = Message(messageObj.getString("text"), "bot", "", "")
                                insertMessage(m)

                                var buttons = messageObj.getJSONArray("buttons")
                                for (b in 0 until buttons.length()) {
                                    var m = Message(
                                            buttons.getJSONObject(b).getString("title"),
                                            "button",
                                            buttons.getJSONObject(b).getString("type"),
                                            buttons.getJSONObject(b).getString("value"))
                                    insertMessage(m)
                                }
                                mLayoutManager.scrollToPosition(itemCount - 1)
                            }
                            else -> {
                                Log.d("TAG", "cards")
                                var cards = messageObj.getJSONArray("cards")
                                var count = cards.length()
                                for (c in 0 until cards.length()){
                                    var m = Message(cards.getJSONObject(c).getString("title"), "cardInfo", cards.getJSONObject(c).getString("subtitle"), cards.getJSONObject(c).getString("image_url"))
                                    insertMessage(m)

                                    var cardButtons = cards.getJSONObject(c).getJSONArray("buttons")
                                    count += cardButtons.length()
                                    for (b in 0 until cardButtons.length()){
                                        var m = Message(cardButtons.getJSONObject(b).getString("title"), "cardButton", cardButtons.getJSONObject(b).getString("type"), cardButtons.getJSONObject(b).getString("value"))
                                        insertMessage(m)
                                    }
                                }
                                //mLayoutManager.scrollToPosition(itemCount - 1)
                                (mLayoutManager.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(itemCount-1-count, 0)
                            }
                        }
                    }

                } else {
                    Log.e("Error", response.code().toString())
                }
            }
        }
    }

    private fun getTtsApi(str: String){
        val speechText = str
        val api = Retrofit.Builder()
                .baseUrl("https://cs-ambassador.herokuapp.com")
                .build()
                .create(Api::class.java)
        val jsonObject = JSONObject()
        jsonObject.put("text", speechText)
        val jsonObjectString = jsonObject.toString()
        val requestBody = jsonObjectString.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        Log.d("jsonObjectString", jsonObjectString)

        CoroutineScope(Dispatchers.IO).launch {
            val response = api.getTTS(requestBody)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val responseString = response.body()?.string()
                    val result = JSONObject(responseString)
                    val speechUrl = result.getString("data")
                    Log.d("speechUrl", speechUrl)
                    getSpeech(speechUrl)
                }
                else {
                    Log.e("Error", "")
                }
            }
        }
    }

    private fun getSpeech(str: String){
        var mp: MediaPlayer = MediaPlayer()
        try {
            mp.setDataSource(str.replace("http://", "https://"))
            mp.prepare()
            mp.start()
        } catch (e: IOException) {
            Log.d("error", "")
            e.printStackTrace()
        }
    }




}