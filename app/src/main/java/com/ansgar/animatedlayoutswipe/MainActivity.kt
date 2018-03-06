package com.ansgar.animatedlayoutswipe

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
    }

    private fun initRecyclerView() = with(recycler_view) {
        layoutManager = CustomLayoutManager(context)
        adapter = TestRecycler(getItems(), listener)
        adapter.notifyDataSetChanged()
    }

    private fun getItems(): List<TestItems> {
        var items = ArrayList<TestItems>()
        (0..200).mapTo(items) { TestItems(it, "Name: $it") }
        return items
    }

    private val listener = object : TestRecycler.RecyclerListener {
        override fun enableRecycleViewScroll(enable: Boolean) {
            (recycler_view.layoutManager as CustomLayoutManager).scrollEnabled = enable
            Log.i("Scroll", "Enable: $enable")
        }
    }
}
