package com.example.zeeshan.zammediaplayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

/**
 * Created by zeeshan on 11/22/2017.
 */

class SongsListAdopter(private val context: Context, private val songs_Name: ArrayList<String>, private val songs_Titles: ArrayList<String>, private val songs_Path: ArrayList<Uri>, private val bitmaps: ArrayList<Bitmap>) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private var mSelectedItem = 0
    private val TAG_UNSELECTED = 0

    fun selectItem(position: Int) {
        mSelectedItem = position
        notifyDataSetChanged()
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mSelectedItem) TAG_SELECTED else TAG_UNSELECTED
    }

    override fun getCount(): Int {
        return songs_Path.size
    }

    override fun getItem(i: Int): Any {
        return songs_Path[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    override fun getView(i: Int, p1: View?, p2: ViewGroup?): View {
        var view = p1

        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        view = layoutInflater!!.inflate(R.layout.row_songs, null)

        val name = view.findViewById<View>(R.id.row_Song_Name) as TextView
        val title = view.findViewById<View>(R.id.row_Song_Title) as TextView
        val image = view.findViewById<View>(R.id.row_image) as ImageView

        name.text = songs_Name[i]
        title.text = songs_Titles[i]
        image.setImageBitmap(bitmaps[i])


        if (MainActivity.instace!!.sharedPreferences.getInt("song_pos", -1) == i && MainActivity.instace!!.sharedPreferences.getInt("song_pos", -1) !== -1) {
            name.setTextColor(Color.GREEN)
            title.setTextColor(Color.GREEN)
            name.isSelected = true
            title.isSelected = true
        } else {
            if (MainActivity.instace!!.sharedPreferences.getInt("song_pos", -1) !== -1) {
                name.setTextColor(Color.WHITE)
                title.setTextColor(Color.WHITE)
                name.isSelected = false
                title.isSelected = false
            }
        }
        val type = getItemViewType(i)


        /*if(type == TAG_SELECTED) {
            view.setBackgroundColor(Color.parseColor("#1da7ff"));
            name.setTextColor(Color.parseColor("#000000"));
            title.setTextColor(Color.parseColor("#000000"));
            name.setSelected(true);
        } else {
            view.setBackgroundColor(Color.parseColor("#f8f8f8"));
            name.setTextColor(Color.parseColor("#FFFFFF"));
            title.setTextColor(Color.parseColor("#FFFFFF"));
        }*/

        return view
    }

    companion object {
        private val TAG_SELECTED = 1
    }


}
