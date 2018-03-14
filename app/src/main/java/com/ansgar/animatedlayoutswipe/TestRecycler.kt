package com.ansgar.animatedlayoutswipe

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.item_test.view.*

/**
 * Created by kirill on 4.3.18.
 */
class TestRecycler(private val items: List<TestItems>, private val listener: RecyclerListener) : RecyclerView.Adapter<TestRecycler.TestHolder>() {

    private var parent: ViewGroup? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: TestHolder, position: Int) {
        val testItem = items[position]
        holder.bindView(testItem)
        holder.itemView.view.setOnLongClickListener {
            showPopup(it, it.left, (holder.itemView.y + 50).toInt(), 0)
            true
        }
        holder.itemView.image.setOnLongClickListener {
            val offset: Int = holder.itemView.view.left + holder.itemView.txt_tv.width
            val rect = Rect()
            holder.itemView.view.getGlobalVisibleRect(rect)
            showPopup(it, holder.itemView.view.left, rect.top - rect.height() * 2, offset)
            true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_test, parent, false)
        this.parent = parent
        return TestHolder(view)
    }

    override fun getItemCount(): Int = items.size

    inner class TestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(item: TestItems) = with(itemView) {
            number_tv.text = item.id.toString()
            txt_tv.text = item.name
        }
    }

    interface RecyclerListener {
        fun enableRecycleViewScroll(disable: Boolean)
    }


    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showPopup(anchor: View, x: Int, y: Int, offset: Int) {
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(R.layout.linear_layout, null)
        view.animation = AnimationUtils.loadAnimation(parent?.context, R.animator.open_popup_animation)
        val popupMenu = EmojiPopupWindow(view as RelativeLayout, R.id.linear_ll, R.id.background)
        popupMenu.elevation = 10f
        popupMenu.offset = offset
        popupMenu.onMenuItemSelectedListener = object : EmojiPopupWindow.OnMenuItemSelectedListener {

            override fun onPopupOpened() {
                listener.enableRecycleViewScroll(false)
            }

            override fun onPopupDismissed() {
                listener.enableRecycleViewScroll(true)
            }

            override fun onItemSelected(position: Int, view: View) {

            }
        }
        anchor.setOnTouchListener(popupMenu)
        popupMenu.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }

}