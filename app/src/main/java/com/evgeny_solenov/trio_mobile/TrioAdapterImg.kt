package com.example.testandro6

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView


class TrioAdapterImg(var context: Context, var imgs: ArrayList<Int>, var items: ArrayList<String>) :
    BaseAdapter() {
    var inflter: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(i: Int): Any? {
        return null
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        // view = convertView
        var view = inflter.inflate(R.layout.custom_list_item, parent, false)
        val icon = view.findViewById<View>(R.id.iv1) as ImageView
        val names = view.findViewById<View>(R.id.tv1) as TextView
        icon.setImageResource(imgs[i])
        names.text = items[i]
        return view
    }
}