package com.xuyang.a202305100227.Myproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuyang.a202305100227.Myproject.ShopModel
import com.xuyang.a202305100227.Myproject.databinding.ItemEatOutBinding

/**
 * 极简版适配器：仅支持地图图标点击 + 基础数据展示
 * 适配布局：item_eat_out.xml
 */
class ShopAdapter : ListAdapter<ShopModel, ShopAdapter.ShopViewHolder>(ShopDiffCallback()) {

    // 地图图标点击回调（核心）
    private var onLocationIconClick: ((ShopModel) -> Unit)? = null

    // 对外设置点击监听
    fun setOnLocationIconClickListener(listener: (ShopModel) -> Unit) {
        this.onLocationIconClick = listener
    }

    // ViewHolder：绑定item_eat_out.xml布局
    inner class ShopViewHolder(private val binding: ItemEatOutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(shop: ShopModel) {
            // 1. 展示数据（匹配item_eat_out.xml的控件ID）
            binding.tvAddress.text = "${shop.province} ${shop.city} ${shop.area} ${shop.address}"
            binding.tvName.text = shop.name

            // 2. 地图图标点击事件 - 传递完整的shop信息
            binding.ibLocation.setOnClickListener {
                onLocationIconClick?.invoke(shop)
            }
        }
    }

    // 创建ViewHolder：加载item_eat_out.xml布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemEatOutBinding.inflate( // 关键：改为ItemEatOutBinding
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopViewHolder(binding)
    }

    // 绑定数据
    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 极简版DiffUtil：优化列表刷新
    private class ShopDiffCallback : DiffUtil.ItemCallback<ShopModel>() {
        override fun areItemsTheSame(oldItem: ShopModel, newItem: ShopModel): Boolean {
            return oldItem.name == newItem.name && oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: ShopModel, newItem: ShopModel): Boolean {
            return oldItem == newItem
        }
    }
}