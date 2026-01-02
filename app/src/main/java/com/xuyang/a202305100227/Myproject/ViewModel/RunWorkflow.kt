package com.xuyang.a202305100227.Myproject.ViewModel

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object RunWorkflow {
    private const val COZE_WORKFLOW_URL = "https://api.coze.cn/v1/workflow/run"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 通用调用扣子工作流方法（明确配置所有必填请求头）
     * @param accessToken 令牌（用于拼接 Authorization 头）
     * @param workflowId 工作流 ID
     * @param parameters 动态业务参数
     * @param onSuccess 成功回调
     * @param onError 失败回调
     */
    fun runWorkflow(
        accessToken: String,
        workflowId: String,
        parameters: JSONObject,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                // 构建请求体
                val requestJson = JSONObject()
                requestJson.put("workflow_id", workflowId)
                requestJson.put("parameters", parameters)

                val finalRequestStr = requestJson.toString()
                Log.d("CozeRequest", "修复后请求体：$finalRequestStr")

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = finalRequestStr.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(COZE_WORKFLOW_URL)
                    // 1. Authorization 头：Bearer + Access_Token（身份验证）
                    .addHeader("Authorization", "Bearer $accessToken")
                    // 2. Content-Type 头：明确指定 utf-8，修复编码序列化问题
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .post(requestBody)
                    .build()

                // 发起请求
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                mainHandler.post {
                    when {
                        !response.isSuccessful -> {
                            onError("HTTP 错误：${response.code}，详情：${responseBody ?: "无"}")
                        }
                        responseBody.isNullOrEmpty() -> {
                            onError("响应体为空，请求未获取到结果")
                        }
                        else -> {
                            val responseJson = JSONObject(responseBody)
                            if (responseJson.getLong("code") == 0L) {
                                onSuccess(responseJson.optString("data"))
                            } else {
                                onError("调用失败：${responseJson.optString("msg", "未知错误")}")
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                mainHandler.post {
                    onError("请求异常：${e.message ?: "网络连接失败"}")
                }
                e.printStackTrace()
            }
        }.start()
    }

    fun buildJsonArrayFromList(list: List<String>): JSONArray {
        val jsonArray = JSONArray()
        list.forEach { item -> jsonArray.put(item) }
        return jsonArray
    }

    fun clearCallbacks() {
        mainHandler.removeCallbacksAndMessages(null)
    }
}