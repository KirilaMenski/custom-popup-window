package com.ansgar.animatedlayoutswipe

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.LinearLayout
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

            val inflater = LayoutInflater.from(parent?.context)
            val view = inflater.inflate(R.layout.animated_layout, null)
            val popupMenu = CustomPopupWindow(view as LinearLayout)
            popupMenu.elevation = 10f
            popupMenu.onMenuItemSelectedListener = object : CustomPopupWindow.OnMenuItemSelectedListener {
                override fun menuOpened() {
                    listener.enableRecycleViewScroll(false)
                }

                override fun menuDismissed() {
                    listener.enableRecycleViewScroll(true)
                }

                override fun itemSelected(position: Int, view: View) {
                    Log.i("!!!!", "Item selected: $position, ${view.id};")
                }
            }
            it.setOnTouchListener(popupMenu)
            popupMenu.showAtLocation(it, Gravity.NO_GRAVITY, it.left, (holder.itemView.y+100).toInt())

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

}