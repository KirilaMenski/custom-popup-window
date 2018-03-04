package com.ansgar.animatedlayoutswipe

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import kotlinx.android.synthetic.main.item_test.view.*

/**
 * Created by kirill on 4.3.18.
 */
class TestRecycler(private val items: List<TestItems>, private val listener: RecyclerListener) : RecyclerView.Adapter<TestRecycler.TestHolder>() {

    private var parent: ViewGroup? = null
    private var gestureDetector: GestureDetector? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: TestHolder, position: Int) {
        val testItem = items[position]
        holder.bindView(testItem)
        holder.itemView.view.setOnLongClickListener {
            Log.i("GestureListener", "Disable recycler view scrolling")
            listener.disableRecycleViewScroll(false)

            val inflater = LayoutInflater.from(parent?.context)
            val view = inflater.inflate(R.layout.animated_layout, null)
            val popupMenu = CustomPopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            popupMenu.elevation = 10f
            popupMenu.showAsDropDown(holder.itemView.view)
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
        fun disableRecycleViewScroll(disable: Boolean)
    }

}