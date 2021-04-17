package com.example.hkucsambassador.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {

    @POST("/webhook")
    suspend fun getMessage(@Body requestBody: RequestBody): Response<ResponseBody>

    //@POST("/stt_binary?access_token=cs-ambassador")
    //suspend fun getSTT(@Body requestBody: RequestBody): Response<ResponseBody>

}