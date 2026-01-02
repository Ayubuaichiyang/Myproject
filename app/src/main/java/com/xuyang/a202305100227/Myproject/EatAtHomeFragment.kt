package com.xuyang.a202305100227.Myproject

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.xuyang.a202305100227.Myproject.ViewModel.RunWorkflow
import org.json.JSONObject

// 分类数据模型
data class CategoryModel(
    val title: String,
    val items: List<String>,
    val flexboxId: Int
)

class EatAtHomeFragment : Fragment() {
    // **************** 你的令牌与工作流ID ****************
    private val ACCESS_TOKEN = "pat_uViWgy5whFQpNboFvEm7AsHMdKPVSEXm0hIdL2udxTzAZQswIaB3yDZUVpEkzc6r" // 你的令牌
    private val WORKFLOW_ID = "7590166639191212032" // 你的工作流 ID
    // ***************************************************

    // 选中的食材和烹饪工具
    private val selectedIngredients = mutableSetOf<String>()
    private val selectedCookers = mutableSetOf<String>()
    private lateinit var submit_button: Button
    private lateinit var recipe_result_tv: TextView


    // 所有分类数据
    private val categoryList = listOf(
        CategoryModel("叶菜类", listOf("菠菜", "生菜", "油麦菜", "上海青", "芹菜", "韭菜", "香菜"), R.id.leaf_vegetables_layout),
        CategoryModel("根茎类", listOf("胡萝卜", "白萝卜", "土豆", "山药", "莲藕", "红薯", "洋葱", "大蒜", "生姜"), R.id.root_vegetables_layout),
        CategoryModel("瓜茄类", listOf("黄瓜", "西红柿", "冬瓜", "南瓜", "丝瓜", "茄子", "青椒", "彩椒"), R.id.melon_eggplant_layout),
        CategoryModel("菌菇类", listOf("香菇", "金针菇", "杏鲍菇", "平菇", "木耳", "银耳"), R.id.mushroom_layout),
        CategoryModel("豆制品类", listOf("豆腐", "腐竹", "千张", "豆干", "豆芽"), R.id.bean_product_layout),
        CategoryModel("主食类", listOf("大米", "小米", "鸡蛋", "玉米", "燕麦", "面条", "馒头"), R.id.staple_food_layout),
        CategoryModel("猪肉类", listOf("猪肉", "排骨", "猪蹄", "猪肚", "五花肉", "猪肝", "猪血", "猪腰", "猪皮", "猪肘", "猪耳朵", "猪心", "猪肺", "猪大肠", "猪大骨头", "猪小排", "猪里脊肉", "猪脑", "猪排", "猪舌头", "猪脑袋"), R.id.pork_layout),
        CategoryModel("牛肉类", listOf("牛肉", "牛腩", "牛排", "肥牛", "牛肚", "牛蹄筋", "牛尾", "牛肺", "牛肾", "牛鞭"), R.id.beef_layout),
        CategoryModel("羊肉类", listOf("羊肉", "羊排", "羊肝", "羊肚", "羊蝎子", "羊肾", "羊血", "羊骨"), R.id.lamb_layout),
        CategoryModel("鸡肉类", listOf("鸡肉", "鸡翅", "鸡腿", "鸡爪", "鸡肝", "鸡胗", "鸡血", "鸡心", "火鸡肉"), R.id.chicken_layout),
        CategoryModel("鸭肉类", listOf("鸭肉", "鸭肝", "鸭腿", "鸭翅", "鸭胗", "鸭血", "鸭掌", "鸭肠"), R.id.duck_layout),
        CategoryModel("肉制品类", listOf("腊肉", "火腿", "香肠", "咸肉", "肉松", "培根", "午餐肉", "熏肉", "鸭血糕"), R.id.meat_product_layout),
        CategoryModel("其他肉类", listOf("兔肉", "鹿肉", "驴肉", "鹅肉", "鹅肝"), R.id.other_meat_layout),
        CategoryModel("淡水水产类", listOf("鲫鱼", "鲤鱼", "草鱼", "鲈鱼", "黑鱼", "黄鳝", "泥鳅", "小龙虾", "大闸蟹", "河蟹", "青虾", "田螺", "河蚌"), R.id.freshwater_seafood_layout),
        CategoryModel("海水水产类", listOf("带鱼", "黄花鱼", "三文鱼", "鳕鱼", "龙利鱼", "巴沙鱼", "秋刀鱼", "鱿鱼", "墨鱼", "基围虾", "对虾", "皮皮虾", "梭子蟹", "花甲", "蛏子", "扇贝", "生蚝", "蛤蜊", "海虹", "海蜇", "海带", "紫菜", "海苔", "虾滑"), R.id.seawater_seafood_layout),
        CategoryModel("烹饪工具", listOf("炒锅", "煮锅", "蒸锅", "砂锅", "煎锅", "高压锅", "电饭煲", "电磁炉", "电烤箱", "空气炸锅", "微波炉", "破壁机"), R.id.cooker_layout)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_eat_at_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 返回按钮逻辑
        view.findViewById<View>(R.id.back_button).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 初始化所有分类标签
        initAllCategoryTags()

        submit_button = view.findViewById(R.id.submit_button)
        recipe_result_tv = view.findViewById(R.id.recipe_result_tv)

        // 生成食谱按钮点击
        submit_button.setOnClickListener {
            if (selectedIngredients.isEmpty()) {
                recipe_result_tv.text = "请至少选择一种食材后再生成食谱！"
                return@setOnClickListener
            }

            // 构建动态参数（复用选中的食材/工具）
            val parameters = JSONObject().apply {
                put("ingredients", RunWorkflow.buildJsonArrayFromList(selectedIngredients.toList()))
                put("cookers", RunWorkflow.buildJsonArrayFromList(selectedCookers.toList()))
            }

            // 调用工作流
            RunWorkflow.runWorkflow(
                accessToken = ACCESS_TOKEN,
                workflowId = WORKFLOW_ID,
                parameters = parameters,
                onSuccess = { result ->
                    // 解析结果并展示菜品名称
                    parseDishNamesFromResult(result)
                },
                onError = { errorMsg ->
                    recipe_result_tv.text = "生成失败：$errorMsg"
                }
            )
        }
    }

    /**
     * 解析工作流返回的结果，提取dishes中的dish_name并展示
     */
    private fun parseDishNamesFromResult(result: String?) {
        if (result.isNullOrEmpty()) {
            recipe_result_tv.text = "返回结果为空，无法生成食谱！"
            return
        }
        try {
            // 直接解析data字段的JSON（无需解析外层，因为runWorkflow已提取data）
            val dataObj = JSONObject(result)

            // 检查是否有dishes字段
            if (!dataObj.has("dishes")) {
                recipe_result_tv.text = "返回结果无食谱数据！"
                return
            }

            // 遍历dishes数组，提取所有dish_name
            val dishesArray = dataObj.getJSONArray("dishes")
            val dishNames = mutableListOf<String>()
            for (i in 0 until dishesArray.length()) {
                val dishObj = dishesArray.getJSONObject(i)
                if (dishObj.has("dish_name")) {
                    dishNames.add(dishObj.getString("dish_name"))
                }
            }

            // 展示结果（逗号分隔）
            if (dishNames.isEmpty()) {
                recipe_result_tv.text = "未生成可用食谱，请更换食材重试！"
            } else {
                recipe_result_tv.text = "生成的食谱：${dishNames.joinToString("，")}"
            }

        } catch (e: Exception) {
            // 解析异常处理
            recipe_result_tv.text = "解析失败：${e.message}"
            e.printStackTrace()
        }
    }

    /**
     * 初始化所有分类的标签
     */
    private fun initAllCategoryTags() {
        categoryList.forEach { category ->
            val flexbox = view?.findViewById<FlexboxLayout>(category.flexboxId)
            category.items.forEach { item ->
                flexbox?.addView(createTagView(item, category.title == "烹饪工具"))
            }
        }
    }

    /**
     * 创建单个标签View
     * @param text 标签文字
     * @param isCooker 是否是烹饪工具（区分食材/工具）
     */
    private fun createTagView(text: String, isCooker: Boolean): TextView {
        val tagView = TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setPadding(20, 10, 20, 10)
            // 未选中样式
            setBackgroundColor(Color.parseColor("#E0E0E0"))
            setTextColor(Color.parseColor("#333333"))
            // 设置圆角
            background = resources.getDrawable(R.drawable.tag_bg_unselected, requireContext().theme)
            // 设置间距
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 8, 8, 8)
            this.layoutParams = layoutParams

            // 点击事件：切换选中状态
            setOnClickListener {
                // 切换视图的选中状态
                isSelected = !isSelected
                // 更新样式
                updateTagStyle(this, isSelected)
                // 更新选中集合
                if (isCooker) {
                    if (isSelected) {
                        selectedCookers.add(text)
                    } else {
                        selectedCookers.remove(text)
                    }
                } else {
                    if (isSelected) {
                        selectedIngredients.add(text)
                    } else {
                        selectedIngredients.remove(text)
                    }
                }
            }
        }
        return tagView
    }

    /**
     * 更新标签选中/未选中样式
     * @param tagView 标签View
     * @param isSelected 是否选中
     */
    private fun updateTagStyle(tagView: TextView, isSelected: Boolean) {
        if (isSelected) {
            // 选中样式
            tagView.setBackgroundColor(Color.parseColor("#96CEB4"))
            tagView.setTextColor(Color.WHITE)
            tagView.background = resources.getDrawable(R.drawable.tag_bg_selected, requireContext().theme)
        } else {
            // 未选中样式
            tagView.setBackgroundColor(Color.parseColor("#E0E0E0"))
            tagView.setTextColor(Color.parseColor("#333333"))
            tagView.background = resources.getDrawable(R.drawable.tag_bg_unselected, requireContext().theme)
        }
    }

    /**
     * 生命周期销毁时清空回调，避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        RunWorkflow.clearCallbacks()
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