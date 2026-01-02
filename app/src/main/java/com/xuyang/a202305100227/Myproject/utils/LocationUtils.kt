package com.xuyang.a202305100227.Myproject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * 定位工具类（封装权限申请 + 经纬度获取）
 * 使用说明：
 * 1. 在Activity/Fragment中调用 requestLocationPermission() 申请权限
 * 2. 权限通过后调用 getCurrentLocation() 获取经纬度
 */
object LocationUtils {
    // 权限请求码
    const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    /**
     * 检查定位权限是否已授予
     */
    fun isLocationPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 申请定位权限（Activity中调用）
     */
    fun requestLocationPermission(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * 申请定位权限（Fragment中调用）
     */
    fun requestLocationPermission(fragment: Fragment) {
        fragment.requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * 检查定位服务是否开启
     */
    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 检查GPS或网络定位是否开启
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * 获取当前经纬度（封装核心逻辑）
     * @param context 上下文
     * @param onSuccess 成功回调（纬度，经度）
     * @param onError 失败回调（错误信息）
     */
    fun getCurrentLocation(
        context: Context,
        onSuccess: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. 检查权限
        if (!isLocationPermissionGranted(context)) {
            onError("定位权限未授予，请先申请权限")
            return
        }

        // 2. 检查定位服务是否开启
        if (!isLocationServiceEnabled(context)) {
            onError("定位服务未开启，请前往设置开启")
            return
        }

        // 3. 获取LocationManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 4. 定义LocationListener（监听位置变化）
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // 获取到经纬度后停止监听
                locationManager.removeUpdates(this)
                val latitude = location.latitude // 纬度
                val longitude = location.longitude // 经度
                onSuccess(latitude, longitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                onError("定位服务已被禁用")
                locationManager.removeUpdates(this)
            }
        }

        try {
            // 优先使用GPS定位（精度高）
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
            // GPS不可用时使用网络定位
            else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    locationListener,
                    Looper.getMainLooper()
                )
            } else {
                onError("无可用的定位方式")
            }
        } catch (e: SecurityException) {
            onError("定位权限异常：${e.message}")
        } catch (e: Exception) {
            onError("获取定位失败：${e.message}")
        }
    }
}