package com.example.zeeshan.zammediaplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.RemoteViews
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.util.ArrayList


import dyanamitechetan.vusikview.VusikView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener {
    var songs_Name: ArrayList<String>? = null
        private set
    var songs_Title: ArrayList<String>? = null
        private set
    var paths: ArrayList<Uri>? = null
        private set
    private var songs: ArrayList<File>? = null
    internal var art: ByteArray? = null
    internal var bitmaps=ArrayList<Bitmap>();
    internal lateinit var  listView:ListView
    internal lateinit var play: ImageButton
    internal lateinit var next: ImageButton
    internal lateinit var back: ImageButton
    private val isPlaying: Boolean = false
    internal lateinit var seekBar: SeekBar
    internal var realTimeLength = 1000
    private var isBind: Boolean = false
    internal var handler: Handler = Handler()
    internal var handler1: Handler= Handler()
    internal var handler2: Handler= Handler()
    internal var handler3: Handler= Handler()
    private var imageView: ImageView? = null
    internal lateinit var song_Loop: ImageView
    private var name: TextView? = null
    private var title: TextView? = null
    private var song_Current_Duration: TextView? = null
    private var song_Total_Duration: TextView? = null
    internal lateinit var runnable: Runnable
    internal lateinit var sharedPreferences: SharedPreferences
    internal lateinit var relativeLayout: RelativeLayout
    internal lateinit var showDuarationRelativeLayout: RelativeLayout
    internal lateinit var params: RelativeLayout.LayoutParams
    internal lateinit var mediaMetadataRetriever: MediaMetadataRetriever
    internal lateinit var songBackGroundService: SongBackGroundService
    private var vusikView: VusikView? = null
    private var actionBar: android.support.v7.app.ActionBar? = null
    internal var songsListAdopter: SongsListAdopter?= null;
    internal lateinit var editor1: SharedPreferences.Editor

    var viewRow: View? = null

    internal var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val myBinder = iBinder as SongBackGroundService.MyBinder
            songBackGroundService = myBinder.service
            isBind = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instace = this
        songs_Name = ArrayList()
        songs_Title = ArrayList()
        paths = ArrayList()
        songs = ArrayList()
        bitmaps = ArrayList()
        vusikView = findViewById<View>(R.id.vusik) as VusikView
        listView = findViewById<View>(R.id.songs_listView) as ListView
        play = findViewById<View>(R.id.song_Play) as ImageButton
        next = findViewById<View>(R.id.song_next) as ImageButton
        back = findViewById<View>(R.id.song_Reverse) as ImageButton
        seekBar = findViewById<View>(R.id.seek_Bar) as SeekBar
        name = findViewById<View>(R.id.song_name) as TextView
        imageView = findViewById<View>(R.id.image_song) as ImageView
        song_Loop = findViewById<View>(R.id.song_Loop) as ImageView
        name!!.isSelected = true
        title = findViewById<View>(R.id.song_Title) as TextView
        song_Current_Duration = findViewById<View>(R.id.song_Current_Duration) as TextView
        song_Total_Duration = findViewById<View>(R.id.song_Total_Duration) as TextView
        handler = Handler()

        handler2 = Handler()
        handler3 = Handler()
        relativeLayout = findViewById<View>(R.id.lin) as RelativeLayout
        showDuarationRelativeLayout = findViewById<View>(R.id.time_Duration) as RelativeLayout
        params = relativeLayout.layoutParams as RelativeLayout.LayoutParams
        mediaMetadataRetriever = MediaMetadataRetriever()
        val intent = Intent(this@MainActivity, SongBackGroundService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        editor1 = sharedPreferences.edit()
        val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val componentName = ComponentName(packageName, HeadphoneButtonBroadCaster::class.java.getCanonicalName())
        mAudioManager.registerMediaButtonEventReceiver(componentName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                songs = getSongs(Environment.getExternalStorageDirectory())


                if (songs!!.size > 0) {

                    val runnableSongs = Runnable {
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 6
                        for (i in 0..songs!!.size - 1) {

                            mediaMetadataRetriever.setDataSource(songs!![i].toString())
                            val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            val artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

                            if (title != null) {
                                songs_Name!!.add(title)
                            } else {
                                songs_Name!!.add(songs!![i].name.toString().replace(".mp3", ""))
                            }
                            if (artist != null) {
                                songs_Title!!.add(artist)
                            } else {
                                songs_Title!!.add("UNKNOWN")
                            }

                            paths!!.add(Uri.parse(songs!![i].toString()))

                            art = mediaMetadataRetriever.embeddedPicture
                            if (art != null) {
                                var bitmap: Bitmap? = BitmapFactory.decodeByteArray(art, 0, art!!.size, options)
                                bitmaps.add(bitmap!!)
                                if (bitmap != null) {
                                    bitmap = null
                                }
                            } else {
                                bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.row_image))
                            }
                        }
                    }
                    handler3.post(runnableSongs)
                    songsListAdopter = SongsListAdopter(this@MainActivity, songs_Name!!, songs_Title!!, paths!!, bitmaps)
                    listView.adapter = songsListAdopter



                    val runnable1 = Runnable {
                        if (songBackGroundService.mediaPlayer != null && songBackGroundService.mediaPlayer.isPlaying) {
                            seekBar.max = songBackGroundService.mediaPlayer.duration
                            updateSeekBar()
                            play.setImageResource(R.drawable.ic_pause)
                            name!!.text = songs_Name!![sharedPreferences.getInt("song_pos", 0)]
                            title!!.text = songs_Title!![sharedPreferences.getInt("song_pos", 0)]
                            imageView!!.setImageBitmap(bitmaps[sharedPreferences.getInt("song_pos", 0)])
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

                        } else {
                            play.setImageResource(R.drawable.ic_play)
                            name!!.text = songs_Name!![sharedPreferences.getInt("song_pos", 0)]
                            title!!.text = songs_Title!![sharedPreferences.getInt("song_pos", 0)]
                            imageView!!.setImageBitmap(bitmaps[sharedPreferences.getInt("song_pos", 0)])
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

                        }
                        listView.smoothScrollToPosition(sharedPreferences.getInt("song_pos", 0))
                    }
                    handler2.postDelayed(runnable1, 500)

                    listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        upDateListColor(view, i)
                        startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", i).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                        play.setImageResource(R.drawable.ic_pause)
                        val runnable1 = Runnable {
                            seekBar.max = songBackGroundService.mediaPlayer.duration
                            updateSeekBar()
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
                        }
                        handler1.postDelayed(runnable1, 500)

                        name!!.text = songs_Name!![i]
                        title!!.text = songs_Title!![i]
                        imageView!!.setImageBitmap(bitmaps[i])
                    }

                } else {
                    Toast.makeText(this@MainActivity, "No Songs Available", Toast.LENGTH_LONG).show()
                }
            }
        } else {

            val musicResolver = contentResolver
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val musicCursor = musicResolver.query(musicUri, null, null, null, null)
            if (musicCursor != null) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 6

                while (musicCursor.moveToNext()) {
                    songs_Name!!.add(musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)).replace(".mp3", ""))
                    paths!!.add(Uri.parse(musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Images.Media.DATA))))
                    songs_Title!!.add(musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Images.Media.TITLE)))
                    mediaMetadataRetriever.setDataSource(musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                    art = mediaMetadataRetriever.embeddedPicture
                    if (art != null) {
                        var bitmap: Bitmap? = BitmapFactory.decodeByteArray(art, 0, art!!.size, options)
                        bitmaps.add(bitmap!!)
                        if (bitmap != null) {
                            bitmap = null
                        }
                    } else {
                        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.row_image))
                    }
                }

                System.gc()

                songsListAdopter = SongsListAdopter(this@MainActivity, songs_Name!!, songs_Title!!, paths!!, bitmaps)
                listView.adapter = songsListAdopter


                val runnable1 = Runnable {
                    if (songBackGroundService.mediaPlayer != null && songBackGroundService.mediaPlayer.isPlaying) {
                        seekBar.max = songBackGroundService.mediaPlayer.duration
                        updateSeekBar()
                        play.setImageResource(R.drawable.ic_pause)
                        name!!.text = songs_Name!![sharedPreferences.getInt("song_pos", 0)]
                        title!!.text = songs_Title!![sharedPreferences.getInt("song_pos", 0)]
                        imageView!!.setImageBitmap(bitmaps[sharedPreferences.getInt("song_pos", 0)])
                        songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)


                    } else {
                        play.setImageResource(R.drawable.ic_play)
                        name!!.text = songs_Name!![sharedPreferences.getInt("song_pos", 0)]
                        title!!.text = songs_Title!![sharedPreferences.getInt("song_pos", 0)]
                        imageView!!.setImageBitmap(bitmaps[sharedPreferences.getInt("song_pos", 0)])
                        songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
                    }


                    listView.setSelection(sharedPreferences.getInt("song_pos", 0))
                }
                handler2.postDelayed(runnable1, 300)


                listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                    editor1.putInt("song_pos", i)
                    editor1.commit()
                    songsListAdopter!!.notifyDataSetChanged()

                    startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", i).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                    play.setImageResource(R.drawable.ic_pause)
                    val runnable1 = Runnable {
                        seekBar.max = songBackGroundService.mediaPlayer.duration
                        updateSeekBar()
                        songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
                    }
                    handler1.postDelayed(runnable1, 200)
                    name!!.text = songs_Name!![i]
                    title!!.text = songs_Title!![i]
                    imageView!!.setImageBitmap(bitmaps[i])
                }
            } else {
                Toast.makeText(this@MainActivity, "No Songs Available", Toast.LENGTH_SHORT).show()
            }
        }

        song_Loop.setOnClickListener {
            if (sharedPreferences.getBoolean("bo", false) == true) {
                editor1.putBoolean("bo", false)
                editor1.commit()
                song_Loop.setImageResource(R.drawable.ic_repeatallt)
            } else {
                editor1.putBoolean("bo", true)
                editor1.commit()
                song_Loop.setImageResource(R.drawable.ic_repeartone)
            }
        }

        play.setOnClickListener {
            val remoteViews = RemoteViews(packageName, R.layout.notifactions)
            if (songBackGroundService.mediaPlayer.isPlaying) {
                songBackGroundService.mediaPlayer.pause()
                play.setImageResource(R.drawable.ic_play)
                remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
                songBackGroundService.notification.contentView = remoteViews
                songBackGroundService.startForeground(1337, songBackGroundService.notification)
            } else {
                if (songBackGroundService.position == 0 && !songBackGroundService.mediaPlayer.isPlaying) {
                    if (songBackGroundService.mediaPlayer.currentPosition > 0) {
                        songBackGroundService.mediaPlayer.start()
                        play.setImageResource(R.drawable.ic_pause)
                        remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
                        songBackGroundService.notification.contentView = remoteViews
                        songBackGroundService.startForeground(1337, songBackGroundService.notification)
                        seekBar.max = songBackGroundService.mediaPlayer.duration
                        updateSeekBar()
                    } else {

                        startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", sharedPreferences.getInt("song_pos", 0)).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                        play.setImageResource(R.drawable.ic_pause)
                        seekBar.max = songBackGroundService.mediaPlayer.duration
                        updateSeekBar()
                    }

                } else {
                    songBackGroundService.mediaPlayer.start()
                    play.setImageResource(R.drawable.ic_pause)
                    remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
                    songBackGroundService.notification.contentView = remoteViews
                    songBackGroundService.startForeground(1337, songBackGroundService.notification)
                    seekBar.max = songBackGroundService.mediaPlayer.duration
                    updateSeekBar()
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {

                    songBackGroundService.mediaPlayer.seekTo(i)
                    play.setImageResource(R.drawable.ic_pause)

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        next.setOnClickListener {
            val pos = sharedPreferences.getInt("song_pos", 0)
            if (pos >= songs_Name!!.size - 1) {
                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![0]
                title!!.text = songs_Title!![0]
                setVusikView(0)
                editor1.putInt("song_pos", 0)
                editor1.commit()
                listView.smoothScrollToPosition(0)
                songsListAdopter!!.notifyDataSetChanged()
            } else {

                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", pos + 1).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![pos + 1]
                title!!.text = songs_Title!![pos + 1]
                imageView!!.setImageBitmap(bitmaps[pos + 1])
                setVusikView(pos + 1)
                editor1.putInt("song_pos", pos + 1)
                editor1.commit()
                listView.smoothScrollToPosition(pos + 1)
                songsListAdopter!!.notifyDataSetChanged()
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

            }
            play.setImageResource(R.drawable.ic_pause)
            val runnable1 = Runnable {
                seekBar.max = songBackGroundService.mediaPlayer.duration
                updateSeekBar()
                song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
            }
            handler1.postDelayed(runnable1, 200)
        }

        back.setOnClickListener {
            val pos = sharedPreferences.getInt("song_pos", 0)
            if (pos == 0) {
                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", songs_Name!!.size - 1).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![songs_Name!!.size - 1]
                title!!.text = songs_Title!![songs_Name!!.size - 1]
                imageView!!.setImageBitmap(bitmaps[songs_Name!!.size - 1])
                setVusikView(songs_Name!!.size - 1)
                editor1.putInt("song_pos", songs_Name!!.size - 1)
                editor1.commit()
                listView.smoothScrollToPosition(songs_Name!!.size - 1)
                songsListAdopter!!.notifyDataSetChanged()
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
            } else {
                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", pos - 1).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![pos - 1]
                title!!.text = songs_Title!![pos - 1]
                imageView!!.setImageBitmap(bitmaps[pos - 1])
                setVusikView(pos - 1)
                editor1.putInt("song_pos", pos - 1)
                editor1.commit()
                listView.smoothScrollToPosition(pos - 1)
                songsListAdopter!!.notifyDataSetChanged()
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
            }
            play.setImageResource(R.drawable.ic_pause)
            val runnable1 = Runnable {
                //Toast.makeText(MainActivity.this,songBackGroundService.getMediaPlayer().getDuration()+"",Toast.LENGTH_SHORT).show();
                seekBar.max = songBackGroundService.mediaPlayer.duration
                updateSeekBar()
                song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
            }
            handler1.postDelayed(runnable1, 200)
        }


        name!!.setOnClickListener {
            listView.visibility = View.GONE
            actionBar = supportActionBar
            actionBar!!.hide()
            params.height = 680
            if (songBackGroundService.mediaPlayer.isPlaying) {
                song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
            }

            if (sharedPreferences.getBoolean("bo", false) == true) {
                song_Loop.setImageResource(R.drawable.ic_repeartone)
            } else {
                song_Loop.setImageResource(R.drawable.ic_repeatallt)
            }

            showDuarationRelativeLayout.visibility = View.VISIBLE
            if (vusikView!!.visibility == View.GONE) {
                vusikView!!.visibility = View.VISIBLE
                val bitmap: Bitmap
                mediaMetadataRetriever.setDataSource(paths!![songBackGroundService.position].toString())
                art = mediaMetadataRetriever.embeddedPicture
                if (art != null) {
                    bitmap = BitmapFactory.decodeByteArray(art, 0, art!!.size)


                } else {
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.row_image)
                }
                val d = BitmapDrawable(resources, bitmap)
                vusikView!!.background = d
                vusikView!!.start()

            }
        }

        title!!.setOnClickListener {
            listView.visibility = View.GONE
            actionBar = supportActionBar
            actionBar!!.hide()
            params.height = 680
            if (songBackGroundService.mediaPlayer.isPlaying) {
                song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
            }
            if (sharedPreferences.getBoolean("bo", false) == true) {
                song_Loop.setImageResource(R.drawable.ic_repeartone)
            } else {
                song_Loop.setImageResource(R.drawable.ic_repeatallt)
            }
            showDuarationRelativeLayout.visibility = View.VISIBLE
            if (vusikView!!.visibility == View.GONE) {
                vusikView!!.visibility = View.VISIBLE
                val bitmap: Bitmap
                mediaMetadataRetriever.setDataSource(paths!![songBackGroundService.position].toString())
                art = mediaMetadataRetriever.embeddedPicture
                if (art != null) {
                    bitmap = BitmapFactory.decodeByteArray(art, 0, art!!.size)


                } else {
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.row_image)
                }
                val d = BitmapDrawable(resources, bitmap)
                vusikView!!.background = d
                vusikView!!.start()

            }
        }

        imageView!!.setOnClickListener {
            listView.visibility = View.GONE
            actionBar = supportActionBar
            actionBar!!.hide()
            params.height = 680
            if (songBackGroundService.mediaPlayer.isPlaying) {
                song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
            }
            if (sharedPreferences.getBoolean("bo", false) == true) {
                song_Loop.setImageResource(R.drawable.ic_repeartone)
            } else {
                song_Loop.setImageResource(R.drawable.ic_repeatallt)
            }
            showDuarationRelativeLayout.visibility = View.VISIBLE
            if (vusikView!!.visibility == View.GONE) {
                vusikView!!.visibility = View.VISIBLE
                val bitmap: Bitmap
                mediaMetadataRetriever.setDataSource(paths!![songBackGroundService.position].toString())
                art = mediaMetadataRetriever.embeddedPicture
                if (art != null) {
                    bitmap = BitmapFactory.decodeByteArray(art, 0, art!!.size)

                } else {
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.row_image)
                }
                val d = BitmapDrawable(resources, bitmap)
                vusikView!!.background = d
                vusikView!!.start()
            }
        }


    }

    fun upDateListColor(view: View, position: Int) {

        for (i in 0 until listView.childCount) {
            val v = listView.getChildAt(i)
            val txtvietw = v.findViewById<View>(R.id.row_Song_Title) as TextView
            val txtview = v.findViewById<View>(R.id.row_Song_Name) as TextView
            txtview.setTextColor(Color.WHITE)
            txtvietw.setTextColor(Color.WHITE)
            txtvietw.isSelected = false
            txtview.isSelected = false
        }

        val tv = view.findViewById<View>(R.id.row_Song_Name) as TextView
        val tv1 = view.findViewById<View>(R.id.row_Song_Title) as TextView
        tv.setTextColor(Color.GREEN)
        tv1.setTextColor(Color.GREEN)
        tv.isSelected = true
        tv1.isSelected = true
        editor1.putInt("song_pos", position)
        editor1.commit()
    }

    fun updateSeekBar() {
        seekBar.progress = songBackGroundService.mediaPlayer.currentPosition

        if (songBackGroundService.mediaPlayer.isPlaying) {

            runnable = Runnable {
                updateSeekBar()

                song_Current_Duration!!.text = getCurrentDurartion((songBackGroundService.mediaPlayer.currentPosition + 1000).toLong())
            }
            handler.postDelayed(runnable, 1000)

        }

    }

    fun formateMilliSeccond(milliseconds: Long): String {
        var milliseconds = milliseconds
        var finalTimerString = ""
        var secondsString = ""
        milliseconds += 2000
        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds
        } else {
            secondsString = "" + seconds
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString

        return finalTimerString
    }

    fun getCurrentDurartion(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds
        } else {
            secondsString = "" + seconds
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString

        return finalTimerString
    }

    fun getSongs(root: File): ArrayList<File> {
        val songsList = ArrayList<File>()

        val files = root.listFiles()
        if (files != null) {
            for (songs in files) {

                if (songs.isDirectory && !songs.isHidden) {
                    songsList.addAll(getSongs(songs))
                } else {
                    if (songs.name.endsWith(".mp3")) {
                        songsList.add(songs)
                    }
                }

            }
        }

        return songsList
    }


    override fun onCompletion(mediaPlayer: MediaPlayer) {

        if (sharedPreferences.getBoolean("bo", false) == true == true) {
            startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
            name!!.text = songs_Name!![songBackGroundService.position]
            title!!.text = songs_Title!![songBackGroundService.position]
            imageView!!.setImageBitmap(bitmaps[songBackGroundService.position])
            setVusikView(songBackGroundService.position)

            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
        } else {
            if (songBackGroundService.position >= songs_Name!!.size - 1) {
                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![0]
                title!!.text = songs_Title!![0]
                imageView!!.setImageBitmap(bitmaps[0])
                setVusikView(0)

                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

            } else {
                startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position + 1).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                name!!.text = songs_Name!![songBackGroundService.position + 1]
                title!!.text = songs_Title!![songBackGroundService.position + 1]
                imageView!!.setImageBitmap(bitmaps[songBackGroundService.position + 1])
                setVusikView(songBackGroundService.position + 1)
                songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

            }
        }
        val runnable1 = Runnable {
            seekBar.max = songBackGroundService.mediaPlayer.duration
            updateSeekBar()
            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
            song_Total_Duration!!.text = formateMilliSeccond(songBackGroundService.mediaPlayer.duration.toLong())
        }
        handler1.postDelayed(runnable1, 200)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                songs = getSongs(Environment.getExternalStorageDirectory())


                if (songs!!.size > 0) {

                    val runnableSongs = Runnable {
                        val options = BitmapFactory.Options()
                        options.inSampleSize = 6
                        for (i in 0..songs!!.size - 1) {

                            mediaMetadataRetriever.setDataSource(songs!![i].toString())
                            val title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                            val artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

                            if (title != null) {
                                songs_Name!!.add(title)
                            } else {
                                songs_Name!!.add(songs!![i].name.toString().replace(".mp3", ""))
                            }
                            if (artist != null) {
                                songs_Title!!.add(artist)
                            } else {
                                songs_Title!!.add(songs!![i].name.toString().replace(".mp3", ""))
                            }

                            paths!!.add(Uri.parse(songs!![i].toString()))

                            art = mediaMetadataRetriever.embeddedPicture
                            if (art != null) {
                                var bitmap: Bitmap? = BitmapFactory.decodeByteArray(art, 0, art!!.size, options)
                                if (bitmap != null) {
                                    bitmaps.add(bitmap)
                                }
                                if (bitmap != null) {
                                    bitmap = null
                                }
                            } else {
                                bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.row_image))
                            }
                        }
                    }
                    handler3.post(runnableSongs)
                    songsListAdopter = SongsListAdopter(this@MainActivity, songs_Name!!, songs_Title!!, paths!!, bitmaps)
                    listView.adapter = songsListAdopter

                    val runnable1 = Runnable {
                        if (songBackGroundService.mediaPlayer != null && songBackGroundService.mediaPlayer.isPlaying) {
                            seekBar.max = songBackGroundService.mediaPlayer.duration
                            updateSeekBar()
                            play.setImageResource(R.drawable.ic_pause)
                            name!!.text = songs_Name!![songBackGroundService.position]
                            title!!.text = songs_Title!![songBackGroundService.position]
                            imageView!!.setImageBitmap(bitmaps[songBackGroundService.position])
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)

                        } else {

                            play.setImageResource(R.drawable.ic_play)
                            name!!.text = songs_Name!![songBackGroundService.position]
                            title!!.text = songs_Title!![songBackGroundService.position]
                            imageView!!.setImageBitmap(bitmaps[songBackGroundService.position])
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
                        }
                    }
                    handler2.postDelayed(runnable1, 500)

                    listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        startService(Intent(this@MainActivity, SongBackGroundService::class.java).putExtra("pos", i).putExtra("path", paths).putExtra("name", songs_Name).putExtra("title", songs_Title))
                        play.setImageResource(R.drawable.ic_pause)
                        val runnable1 = Runnable {
                            //Toast.makeText(MainActivity.this,songBackGroundService.getMediaPlayer().getDuration()+"",Toast.LENGTH_SHORT).show();
                            seekBar.max = songBackGroundService.mediaPlayer.duration
                            updateSeekBar()
                            songBackGroundService.mediaPlayer.setOnCompletionListener(this@MainActivity)
                        }
                        handler1.postDelayed(runnable1, 500)

                        name!!.text = songs_Name!![i]
                        title!!.text = songs_Title!![i]
                        imageView!!.setImageBitmap(bitmaps[i])
                    }

                } else {
                    Toast.makeText(this@MainActivity, "No Songs Available", Toast.LENGTH_LONG).show()
                }
            } else {
                //not granted
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onBackPressed() {

        if (vusikView!!.visibility == View.GONE) {
            super.onBackPressed()

        } else {

            vusikView!!.stopNotesFall()
            vusikView!!.visibility = View.GONE
            params.height = 552
            showDuarationRelativeLayout.visibility = View.GONE
            listView.visibility = View.VISIBLE
            actionBar!!.show()
        }
    }


    fun updateImageButton(res: Int) {
        this@MainActivity.runOnUiThread { play.setImageResource(res) }
    }

    fun setMaxSeekBar(duration: Int) {
        this@MainActivity.runOnUiThread { seekBar.max = duration }
    }

    fun updateSong_name(song_Name: String) {
        this@MainActivity.runOnUiThread { name!!.text = song_Name }
    }

    fun updateSong_Total_Position(duration: Int) {
        this@MainActivity.runOnUiThread { song_Total_Duration!!.text = formateMilliSeccond(duration.toLong()) }
    }

    fun updateSong_Artist(song_Title: String) {
        this@MainActivity.runOnUiThread { title!!.text = song_Title }
    }


    fun updateSong_Image(pos: Int) {
        this@MainActivity.runOnUiThread { imageView!!.setImageBitmap(bitmaps[pos]) }
    }

    fun updateVusikView(pos: Int) {
        this@MainActivity.runOnUiThread {
            if (vusikView!!.visibility == View.VISIBLE) {
                val bitmap: Bitmap
                mediaMetadataRetriever.setDataSource(paths!![pos].toString())
                art = mediaMetadataRetriever.embeddedPicture
                if (art != null) {
                    bitmap = BitmapFactory.decodeByteArray(art, 0, art!!.size)


                } else {
                    bitmap = BitmapFactory.decodeResource(resources, R.drawable.row_image)
                }
                val d = BitmapDrawable(resources, bitmap)
                vusikView!!.background = d
            }
        }
    }


    fun setVusikView(position: Int) {
        if (vusikView!!.visibility == View.VISIBLE) {
            val bitmap: Bitmap
            mediaMetadataRetriever.setDataSource(paths!![position].toString())
            art = mediaMetadataRetriever.embeddedPicture
            if (art != null) {
                bitmap = BitmapFactory.decodeByteArray(art, 0, art!!.size)


            } else {
                bitmap = BitmapFactory.decodeResource(resources, R.drawable.row_image)
            }
            val d = BitmapDrawable(resources, bitmap)
            vusikView!!.background = d
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instace: MainActivity? = null
            private set
    }

}



