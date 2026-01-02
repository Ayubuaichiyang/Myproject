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
    private val textRadiusRatio = 0.78f
    private val borderWidth = 3f
    private val centerCircleRadius = 36f

    // 转盘扇形颜色
    private val sectorColors = listOf(
        Color.parseColor("#FF6B6B"), // 番茄红
        Color.parseColor("#4ECDC4"), // 清青色
        Color.parseColor("#45B7D1"), // 湖蓝色
        Color.parseColor("#96CEB4"), // 青豆绿
        Color.parseColor("#FFEAA7"), // 鸡蛋黄
        Color.parseColor("#DDA0DD"), // 紫薯紫
        Color.parseColor("#FFB347"), // 南瓜橙
        Color.parseColor("#87CEEB"), // 葱花蓝
        Color.parseColor("#FF69B4"), // 樱桃粉
        Color.parseColor("#98D8C8"), // 菠菜绿
        Color.parseColor("#F7DC6F"), // 玉米黄
        Color.parseColor("#BB8FCE"), // 藕荷紫
        Color.parseColor("#E8C39E"), // 浅棕
        Color.parseColor("#82E0AA"), // 嫩草绿
        Color.parseColor("#F1948A"), // 虾红
        Color.parseColor("#C39BD3"), // 浅紫
        Color.parseColor("#F8C471"), // 姜黄
        Color.parseColor("#58D68D"), // 翠绿
        Color.parseColor("#85C1E9"), // 浅蓝
        Color.parseColor("#FAD7A0"), // 浅橙
        Color.parseColor("#A2D9CE"), // 豆绿
        Color.parseColor("#EB984E"), // 焦糖橙
        Color.parseColor("#76D7C4"), // 薄荷绿
        Color.parseColor("#F5B7B1"), // 浅粉
        Color.parseColor("#D2B4DE"), // 淡紫
        Color.parseColor("#ABEBC6"), // 青柠绿
        Color.parseColor("#F9E79F"), // 淡黄
        Color.parseColor("#88C999"), // 橄榄绿
        Color.parseColor("#FFC0CB"), // 浅粉
        Color.parseColor("#B8F2E6")  // 浅青
    )

    private var sectorList = listOf(
        "番茄炒蛋", "青椒土豆丝", "红烧肉", "清蒸鲈鱼",
        "麻婆豆腐", "可乐鸡翅", "酸辣土豆丝", "清炒西兰花",
        "糖醋里脊", "白灼虾", "香菇青菜", "玉米排骨汤",
        "蛋炒饭", "红烧排骨", "凉拌黄瓜", "清炒四季豆",
        "酸菜鱼", "宫保鸡丁", "拍黄瓜", "红烧茄子",
        "冬瓜海带汤", "青椒炒肉丝", "紫薯粥", "葱油拌面",
        "番茄牛腩", "姜葱炒蟹", "凉拌藕片", "菠菜蛋汤",
        "樱桃肉", "南瓜粥"
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