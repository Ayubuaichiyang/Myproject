package com.xuyang.a202305100227.Myproject

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 常量定义
    private val margin = 20f
    private val textRadiusRatio = 0.65f
    private val borderWidth = 3f
    private val centerCircleRadius = 36f

    // 转盘扇形颜色
    private val sectorColors = listOf(
        Color.parseColor("#FF6B6B"), // 红色
        Color.parseColor("#4ECDC4"), // 青色
        Color.parseColor("#45B7D1"), // 蓝色
        Color.parseColor("#96CEB4"), // 绿色
        Color.parseColor("#FFEAA7"), // 黄色
        Color.parseColor("#DDA0DD"), // 紫色
        Color.parseColor("#FFB347"), // 橙色
        Color.parseColor("#87CEEB"), // 天蓝色
        Color.parseColor("#FF69B4"), // 粉红色
        Color.parseColor("#98D8C8"), // 薄荷绿
        Color.parseColor("#F7DC6F"), // 金黄色
        Color.parseColor("#BB8FCE")  // 淡紫色
    )

    // 菜品选项
    private var sectorList = listOf(
        "中式炒菜", "西式牛排", "日式寿司", "韩式烤肉",
        "意式面条", "泰式咖喱", "墨西哥卷饼", "印度飞饼",
        "粤菜", "川菜", "湘菜", "鲁菜"
    )

    // 公共属性供外部访问
    val sectors: List<String>
        get() = sectorList

    init {
        // 设置文字画笔
        textPaint.color = Color.WHITE
        textPaint.textSize = 28f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true
        
        // 设置文字阴影效果（只在初始化时设置一次）
        textPaint.setShadowLayer(3f, 2f, 2f, Color.BLACK)
    }

//    fun setSectors(newSectors: List<String>) {
//        sectorList = newSectors
//        invalidate() // 重新绘制
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = min(width, height) / 2 - margin
        val centerX = width / 2
        val centerY = height / 2

        // 创建圆形边界
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        val sectorAngle = 360f / sectorList.size

        // 绘制每个扇形区域
        sectorList.forEachIndexed { index, sector ->
            // 设置扇形颜色并绘制扇形
            paint.color = sectorColors[index % sectorColors.size]
            paint.style = Paint.Style.FILL
            canvas.drawArc(rect, index * sectorAngle, sectorAngle, true, paint)

            // 绘制扇形边框
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            canvas.drawArc(rect, index * sectorAngle, sectorAngle, true, paint)

            // 绘制文字
            val textAngle = (index * sectorAngle + sectorAngle / 2).toFloat()
            val textRadius = radius * textRadiusRatio
            val textX = centerX + textRadius * cos(Math.toRadians(textAngle.toDouble())).toFloat()
            val textY = centerY + textRadius * sin(Math.toRadians(textAngle.toDouble())).toFloat()

            canvas.drawText(sector, textX, textY, textPaint)
        }

        // 绘制中心红色圆
        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, centerCircleRadius, paint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(size, size)
    }
}