package com.xuyang.a202305100227.Myproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.UiSettings
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.Marker
import com.amap.api.maps2d.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.xuyang.a202305100227.Myproject.utils.LocationUtils


class ShopMap : Activity(), AMap.OnMapClickListener {
    private var mMapView: MapView? = null
    private var aMap: AMap? = null
    private var uiSettings: UiSettings? = null
    
    // 控制按钮
    private var fabLocation: FloatingActionButton? = null
    private var fabZoomIn: FloatingActionButton? = null
    private var fabZoomOut: FloatingActionButton? = null
    
    // 定位相关
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var isLocationEnabled = false

    // 新增：类级别变量存储商家位置信息，解决作用域问题
    private var mShopLocation: String = ""
    private var mShopName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        // 接收传递的商家信息
        mShopName = intent?.getStringExtra("shop_name") ?: ""
        mShopLocation = intent?.getStringExtra("shop_location") ?: ""
        val shopAddress = intent?.getStringExtra("shop_address") ?: ""
        
        initViews()
        initMap(savedInstanceState, mShopName, mShopLocation, shopAddress)
    }

    /**
     * 初始化视图控件
     */
    private fun initViews() {
        // 获取地图控件引用
        mMapView = findViewById(R.id.map)
        
        // 获取控制按钮
        fabLocation = findViewById(R.id.fab_location)
        fabZoomIn = findViewById(R.id.fab_zoom_in)
        fabZoomOut = findViewById(R.id.fab_zoom_out)
        
        // 设置按钮点击事件
        setupButtonListeners()
    }

    /**
     * 设置按钮点击事件
     */
    private fun setupButtonListeners() {
        // 定位按钮
        fabLocation?.setOnClickListener {
            if (checkLocationPermission()) {
                enableLocation()
            } else {
                requestLocationPermission()
            }
        }
        
        // 放大按钮
        fabZoomIn?.setOnClickListener {
            aMap?.animateCamera(com.amap.api.maps2d.CameraUpdateFactory.zoomIn())
        }
        
        // 缩小按钮
        fabZoomOut?.setOnClickListener {
            aMap?.animateCamera(com.amap.api.maps2d.CameraUpdateFactory.zoomOut())
        }
    }

    /**
     * 初始化地图
     */
    private fun initMap(savedInstanceState: Bundle?, shopName: String, shopLocation: String, shopAddress: String) {
        // 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView?.onCreate(savedInstanceState)
        
        // 获取地图对象
        aMap = mMapView?.map
        
        if (aMap != null) {
            setupMap(shopName, shopLocation, shopAddress)
        }
    }

    /**
     * 配置地图基本设置
     */
    private fun setupMap(shopName: String, shopLocation: String, shopAddress: String) {
        uiSettings = aMap?.uiSettings
        
        // 设置地图点击监听
        aMap?.setOnMapClickListener(this)
        
        // 配置UI设置
        uiSettings?.apply {
            // 显示缩放按钮（默认已有，这里可以控制显示/隐藏）
            isZoomControlsEnabled = false // 使用自定义按钮，隐藏默认按钮
            
            // 显示指南针
            isCompassEnabled = true
            
            // 显示定位按钮
            isMyLocationButtonEnabled = false // 使用自定义定位按钮
            
            // 显示比例尺
            isScaleControlsEnabled = true

        }
        
        // 设置地图位置
        if (shopLocation.isNotEmpty()) {
            // 如果传入了商家位置，将地图中心设置为该位置
            try {
                val coordinates = shopLocation.split(",")
                if (coordinates.size == 2) {
                    val latitude = coordinates[0].toDouble()
                    val longitude = coordinates[1].toDouble()
                    val shopLatLng = LatLng(latitude, longitude)
                    
                    // 移动地图到商家位置，缩放级别为16
                    aMap?.animateCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(shopLatLng, 16f))
                    
                    // 添加商家标记点
                    addShopMarker(shopLatLng, shopName, shopAddress)
                    
                    // 显示商家信息提示
                    if (shopName.isNotEmpty()) {
                        Toast.makeText(this, "正在显示：$shopName", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 位置格式错误，使用默认位置
                    setDefaultLocation()
                }
            } catch (e: Exception) {
                // 解析位置失败，使用默认位置
                Toast.makeText(this, "位置信息解析失败", Toast.LENGTH_SHORT).show()
                setDefaultLocation()
            }
        } else {
            // 没有商家位置，使用默认位置（北京天安门）
            setDefaultLocation()
        }
    }

    /**
     * 添加商家标记点
     */
    private fun addShopMarker(latLng: LatLng, shopName: String, shopAddress: String) {
        // 先设置自定义InfoWindow适配器
        aMap?.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View {
                // 加载自定义布局
                val view = LayoutInflater.from(this@ShopMap).inflate(
                    R.layout.custom_info_window, 
                    null
                )
                
                // 查找控件并设置数据
                val tvShopName = view.findViewById<TextView>(R.id.tv_shop_name)
                val tvShopAddress = view.findViewById<TextView>(R.id.tv_shop_address)
                
                tvShopName.text = shopName
                tvShopAddress.text = shopAddress
                
                return view
            }
            
            override fun getInfoContents(marker: Marker): View? {
                return null // 使用自定义InfoWindow时返回null
            }
        })
        
        // 创建标记点选项
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(shopName)
            .snippet(shopAddress)
            .draggable(false) // 标记点不可拖拽
        
        // 添加标记点到地图
        val marker = aMap?.addMarker(markerOptions)
        
        // 设置InfoWindow点击监听器
        aMap?.setOnInfoWindowClickListener { clickedMarker ->
            if (clickedMarker == marker) {
                val uri = Uri.parse("geo:${mShopLocation.replace(",", ",")}?q=${shopName}")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
        
        // 默认显示InfoWindow
        marker?.showInfoWindow()
    }

    /**
     * 设置默认地图位置
     */
    private fun setDefaultLocation() {
        val beijing = LatLng(39.9042, 116.4074)
        aMap?.moveCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(beijing, 15f))
    }

    /**
     * 启用定位功能
     */
    private fun enableLocation() {
        if (!checkLocationPermission()) {
            Toast.makeText(this, "需要定位权限才能使用定位功能", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // 在高德地图 SDK v6.x 中，使用不同的定位方式
            // 启用定位图层
            aMap?.isMyLocationEnabled = true
            isLocationEnabled = true
            
            Toast.makeText(this, "正在获取当前位置...", Toast.LENGTH_SHORT).show()
            
            // 使用 LocationUtils 进行定位（结合现有工具类）
            LocationUtils.getCurrentLocation(
                this,
                onSuccess = { latitude, longitude ->
                    // 定位成功，移动地图到当前位置
                    val latLng = LatLng(latitude, longitude)
                    aMap?.animateCamera(com.amap.api.maps2d.CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    Toast.makeText(this, "定位成功: ($latitude, $longitude)", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "定位失败: $error", Toast.LENGTH_SHORT).show()
                }
            )
            
        } catch (e: Exception) {
            Toast.makeText(this, "定位初始化失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 检查定位权限
     */
    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 请求定位权限
     */
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * 处理权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation()
                } else {
                    Toast.makeText(this, "定位权限被拒绝，无法使用定位功能", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 地图点击事件
     */
    override fun onMapClick(latLng: LatLng?) {
        latLng?.let {
            Toast.makeText(this, "点击位置: ${it.latitude}, ${it.longitude}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        // 在activity执行onResume时执行mMapView.onResume()，重新绘制加载地图
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 在activity执行onPause时执行mMapView.onPause()，暂停地图的绘制
        mMapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState()，保存地图当前的状态
        mMapView?.onSaveInstanceState(outState)
    }
}
