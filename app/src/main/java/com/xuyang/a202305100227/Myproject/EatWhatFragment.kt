package com.xuyang.a202305100227.Myproject

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.random.Random

class EatWhatFragment : Fragment() {

    private lateinit var wheelView: WheelView
    private lateinit var pointer: View
    private lateinit var lotteryButton: Button
    private lateinit var foodTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_eat_what, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wheelView = view.findViewById(R.id.wheel)
        pointer = view.findViewById(R.id.pointer)
        lotteryButton = view.findViewById(R.id.wheel_lottery_btn)
        foodTitle = view.findViewById(R.id.food_title)
        foodTitle.text = "今天吃什么"

        lotteryButton.setOnClickListener {
            startLottery()
        }
    }

    private fun startLottery() {
        lotteryButton.isEnabled = false
        lotteryButton.text = "转盘中..."

        val sectors = wheelView.sectors
        if (sectors.isEmpty()) {
            foodTitle.text = "没有可选择的选项"
            resetButton()
            return
        }

        val randomIndex = Random.nextInt(sectors.size)
        val selectedFood = sectors[randomIndex]

        val sectorAngle = 360f / sectors.size
        val sectorCenterAngle = randomIndex * sectorAngle + sectorAngle / 2
        val clockwiseRotation = (sectorCenterAngle - 270f + 360f) % 360f
        val targetRotation = Random.nextInt(3, 6) * 360f + clockwiseRotation

        ObjectAnimator.ofFloat(pointer, "rotation", 0f, targetRotation).apply {
            duration = 3000L
            interpolator = DecelerateInterpolator()
            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    foodTitle.text = "今天吃$selectedFood"
                    resetButton()
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    resetButton()
                }
            })
            start()
        }

    }

    private fun resetButton() {
        lotteryButton.isEnabled = true
        lotteryButton.text = "再来一次"
    }
}