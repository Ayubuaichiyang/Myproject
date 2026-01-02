package com.xuyang.a202305100227.Myproject

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xuyang.a202305100227.Myproject.ViewModel.RunWorkflow
import org.json.JSONArray
import org.json.JSONObject

class EatOutFragment : Fragment() {
    // 令牌和工作流ID
    private val ACCESS_TOKEN =
        "pat_uViWgy5whFQpNboFvEm7AsHMdKPVSEXm0hIdL2udxTzAZQswIaB3yDZUVpEkzc6r"
    private val WORKFLOW_ID = "7590630269180690495"

    // 存储经纬度（纬度在前，经度在后）
    private var currentLocation: String = ""

    // 定位状态控制：避免重复请求
    private var isLocating = false

    // 加载提示控件（可选，用于显示定位中状态）
    private lateinit var progressBar: ProgressBar

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var et_search: TextInputEditText
    private lateinit var iv_search: ImageView

    // 商家列表核心
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var rvAddress: androidx.recyclerview.widget.RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_eat_out, container, false)
        // 初始化所有控件（严格匹配布局ID）
        initViews(view)
        // 初始化商家列表
        initShopList()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 返回按钮逻辑
        view.findViewById<View>(R.id.back_button).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 清空输入框逻辑
        textInputLayout.setEndIconOnClickListener {
            et_search.setText("")
        }

        // 进入页面时自动获取经纬度（仅一次）
        getLocationOnPageEnter()

        // 搜索按钮点击逻辑（添加防抖+状态控制）
        iv_search.setOnClickListener {
            val searchContent = et_search.text?.toString()?.trim() ?: ""

            // 1. 校验搜索内容
            if (searchContent.isEmpty()) {
                Toast.makeText(context, "请输入搜索内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 校验定位状态
            when {
                isLocating -> Toast.makeText(context, "正在获取定位，请稍等...", Toast.LENGTH_SHORT).show()
                currentLocation.isNotEmpty() -> callWorkflow(searchContent)
                else -> {
                    getLocationOnPageEnter()
                    Toast.makeText(context, "正在重新获取定位，请稍等...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 获取经纬度（添加状态控制，避免重复请求）
     */
    private fun getLocationOnPageEnter() {
        if (isLocating) return
        if (!LocationUtils.isLocationPermissionGranted(requireContext())) {
            LocationUtils.requestLocationPermission(this)
            return
        }

        isLocating = true
        progressBar.visibility = View.VISIBLE

        LocationUtils.getCurrentLocation(
            context = requireContext(),
            onSuccess = { latitude, longitude ->
                isLocating = false
                progressBar.visibility = View.GONE
                currentLocation = "$latitude,$longitude"
            },
            onError = { errorMsg ->
                isLocating = false
                progressBar.visibility = View.GONE
                currentLocation = ""
                Toast.makeText(context, "定位失败：$errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * 初始化所有控件（严格匹配布局ID）
     */
    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progress_bar)
        textInputLayout = view.findViewById(R.id.textInputLayout)
        et_search = view.findViewById(R.id.et_search)
        iv_search = view.findViewById(R.id.iv_search)
        rvAddress = view.findViewById(R.id.rv_address)
    }

    /**
     * 初始化商家列表
     */
    private fun initShopList() {
        // 1. 设置布局管理器（垂直列表）
        rvAddress.layoutManager = LinearLayoutManager(requireContext())
        // 2. 初始化Adapter
        shopAdapter = ShopAdapter()
        rvAddress.adapter = shopAdapter

        // 3. 定位图标点击回调（扩展：可跳转到地图）
        shopAdapter.setOnLocationIconClickListener { shop ->
            Toast.makeText(
                context,
                "已定位到：${shop.name}（${shop.location}）",
                Toast.LENGTH_SHORT
            ).show()
            // 可扩展：调用系统地图打开经纬度
            // val uri = Uri.parse("geo:${shop.location.replace(",", ",")}?q=${shop.name}")
            // startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }


    /**
     * 调用工作流（抽离逻辑，避免重复代码）
     */
    private fun callWorkflow(searchContent: String) {
        val parameters = JSONObject().apply {
            put("keyword", searchContent)
            put("location", currentLocation)
        }

        RunWorkflow.runWorkflow(
            accessToken = ACCESS_TOKEN,
            workflowId = WORKFLOW_ID,
            parameters = parameters,
            onSuccess = { result ->
                Log.d("TAG", "原始返回JSON：$result")
                try {

                    val rootJson = JSONObject(result)

                    val results = if (rootJson.has("results") && !rootJson.isNull("results")) {
                        rootJson.getJSONArray("results")
                    } else {
                        JSONArray()
                    }

                    if (results.length() == 0) {
                        Toast.makeText(context, "未找到相关商家", Toast.LENGTH_SHORT).show()
                        shopAdapter.submitList(emptyList())
                        return@runWorkflow
                    }

                    val shopList = mutableListOf<ShopModel>()
                    for (i in 0 until results.length()) {
                        val shopJson = results.getJSONObject(i)

                        val name = if (shopJson.has("name") && !shopJson.isNull("name")) {
                            shopJson.getString("name")
                        } else {
                            "未知商家"
                        }
                        val province = if (shopJson.has("province") && !shopJson.isNull("province")) shopJson.getString("province") else ""
                        val city = if (shopJson.has("city") && !shopJson.isNull("city")) shopJson.getString("city") else ""
                        val area = if (shopJson.has("area") && !shopJson.isNull("area")) shopJson.getString("area") else ""
                        val address = if (shopJson.has("address") && !shopJson.isNull("address")) shopJson.getString("address") else ""
                        val location = if (shopJson.has("location") && !shopJson.isNull("location")) shopJson.getString("location") else ""

                        shopList.add(ShopModel(
                            name = name,
                            province = province,
                            city = city,
                            area = area,
                            address = address,
                            location = location
                        ))
                    }

                    shopAdapter.submitList(shopList)
                    Toast.makeText(context, "找到${shopList.size}家相关商家", Toast.LENGTH_SHORT).show()

                } catch (e: org.json.JSONException) {
                    Toast.makeText(context, "JSON解析失败：${e.message}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "数据处理失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { errorMsg ->
                Toast.makeText(context, "搜索失败：$errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * 处理定位权限申请结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationUtils.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                getLocationOnPageEnter()
            } else {
                isLocating = false
                progressBar.visibility = View.GONE
                currentLocation = ""
                Toast.makeText(context, "定位权限被拒绝，无法获取位置信息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideBottomNavigation()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.showBottomNavigation()
    }
}

// 商家数据模型（保持不变）
data class ShopModel(
    val name: String,
    val province: String,
    val city: String,
    val area: String,
    val address: String,
    val location: String
)