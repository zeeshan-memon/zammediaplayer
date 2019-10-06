package com.example.zeeshan.zammediaplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.provider.SyncStateContract
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast

import java.io.IOException
import java.util.ArrayList

/**
 * Created by zeeshan on 11/22/2017.
 */

class SongBackGroundService : Service() {

    lateinit var mediaPlayer: MediaPlayer
    private val isPlaying = true
    internal lateinit var handler: Handler
    internal lateinit var notification: Notification

    var position: Int = 0

    var path: ArrayList<Uri>? = null

    var song_Name: ArrayList<String>? = null

    var song_Title: ArrayList<String>? = null


    private val myBinder = MyBinder()
    internal lateinit var notificationView: RemoteViews
    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        path = ArrayList()
        handler = Handler()


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val bundle = intent.extras

        path = bundle!!.getParcelableArrayList("path")
        position = bundle.getInt("pos")
        song_Name = bundle.getStringArrayList("name")
        song_Title = bundle.getStringArrayList("title")
        path = path
        song_Name = song_Name
        song_Title = song_Title
        play(path, position)


        return Service.START_NOT_STICKY

    }

    private fun play(paths: ArrayList<Uri>?, position: Int) {

        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            mediaPlayer = MediaPlayer.create(this, paths!![position])
            mediaPlayer.start()
            mediaPlayer = mediaPlayer
            this.position = position


        } else {
            mediaPlayer = MediaPlayer.create(this, paths!![position])
            mediaPlayer.start()
            mediaPlayer = mediaPlayer
            this.position = position
        }
        notificationView = RemoteViews(this.packageName, R.layout.notifactions)
        val i = Intent(this, MainActivity::class.java)

        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pi = PendingIntent.getActivity(this, 0,
                i, 0)

        // And now, building and attaching the Close button.
        val buttonCloseIntent = Intent(this, NotificationCloseButtonHandler::class.java)

        val buttonClosePendingIntent = PendingIntent.getBroadcast(this, 0, buttonCloseIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        notificationView.setOnClickPendingIntent(R.id.notification_button_close, buttonClosePendingIntent)


        // And now, building and attaching the play button.
        val buttonPlayIntent = Intent(this, NotificationPlayButtonHandler::class.java)

        val buttonPlayPendingIntent = PendingIntent.getBroadcast(this, 0, buttonPlayIntent, 0)
        notificationView.setOnClickPendingIntent(R.id.notification_Song_Play, buttonPlayPendingIntent)


        // And now, building and attaching the next button.
        val buttonNextIntent = Intent(this, NotificationNextButtonHandler::class.java)

        val buttonNextPendingIntent = PendingIntent.getBroadcast(this, 0, buttonNextIntent, 0)
        notificationView.setOnClickPendingIntent(R.id.notification_Song_next, buttonNextPendingIntent)


        // And now, building and attaching the previous button.
        val buttonPreviousIntent = Intent(this, NotificationPreviousButtonHandler::class.java)

        val buttonPreviouPendingIntent = PendingIntent.getBroadcast(this, 0, buttonPreviousIntent, 0)
        notificationView.setOnClickPendingIntent(R.id.notification_Song_Reverse, buttonPreviouPendingIntent)
        if (mediaPlayer.isPlaying) {
            notificationView.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
        } else {
            notificationView.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
        }
        notificationView.setTextViewText(R.id.notification_text_title, song_Name!![position])
        notificationView.setTextViewText(R.id.notification_text_artist, song_Title!![position])
        /*  notificationView.setImageViewBitmap(R.id.notification_Pic,bitmaps.get(position));*/
        notification = NotificationCompat.Builder(applicationContext)
                .setContentTitle("Zam Music Player")
                .setTicker("Zam Music Player")
                .setContentText("Zam Music")
                .setSmallIcon(R.drawable.ic_play)
                .setContent(notificationView)
                .setContentIntent(pi)
                .setOngoing(true).build()

        startForeground(1337, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    inner class MyBinder : Binder() {

        val service: SongBackGroundService
            get() = this@SongBackGroundService
    }


    class NotificationCloseButtonHandler : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val service = Intent(context, SongBackGroundService::class.java)
            context.stopService(service)
            val pid = android.os.Process.myPid()
            android.os.Process.killProcess(pid)
        }
    }


    class NotificationNextButtonHandler : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val binder = peekService(context, Intent(context, SongBackGroundService::class.java))
            if (binder != null) {
                val songBackGroundService = (binder as MyBinder).service

                if (songBackGroundService.position >= songBackGroundService.path!!.size - 1) {
                    context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                    MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![0])
                    MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![0])
                    MainActivity.instace!!.updateVusikView(0)
                    MainActivity.instace!!.updateSong_Image(0)
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                    MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                    MainActivity.instace!!.updateSeekBar()
                    MainActivity.instace!!.editor1.putInt("song_pos", 0)
                    MainActivity.instace!!.editor1.commit()
                    MainActivity.instace!!.listView.smoothScrollToPosition(0)
                    MainActivity.instace!!.songsListAdopter!!.notifyDataSetChanged()
                } else {
                    context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position + 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                    MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.position + 1])
                    MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.position + 1])
                    MainActivity.instace!!.updateVusikView(songBackGroundService.position + 1)
                    MainActivity.instace!!.updateSong_Image(songBackGroundService.position + 1)
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                    MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                    MainActivity.instace!!.updateSeekBar()
                    MainActivity.instace!!.editor1.putInt("song_pos", songBackGroundService.position + 1)
                    MainActivity.instace!!.editor1.commit()
                    MainActivity.instace!!.listView.smoothScrollToPosition(songBackGroundService.position + 1)
                    MainActivity.instace!!.songsListAdopter!!.notifyDataSetChanged()
                }
                MainActivity.instace!!.updateSong_Total_Position(songBackGroundService.mediaPlayer.duration)
            }


        }
    }

    class NotificationPreviousButtonHandler : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val binder = peekService(context, Intent(context, SongBackGroundService::class.java))
            if (binder != null) {
                val songBackGroundService = (binder as MyBinder).service

                if (songBackGroundService.position <= 0) {
                    context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.song_Name!!.size - 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                    MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.song_Name!!.size - 1])
                    MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.song_Name!!.size - 1])
                    MainActivity.instace!!.updateVusikView(songBackGroundService.song_Name!!.size - 1)
                    MainActivity.instace!!.updateSong_Image(songBackGroundService.song_Name!!.size - 1)
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                    MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                    MainActivity.instace!!.updateSeekBar()
                    MainActivity.instace!!.editor1.putInt("song_pos", songBackGroundService.song_Name!!.size - 1)
                    MainActivity.instace!!.editor1.commit()
                    MainActivity.instace!!.listView.smoothScrollToPosition(songBackGroundService.song_Name!!.size - 1)
                    MainActivity.instace!!.songsListAdopter!!.notifyDataSetChanged()
                } else {
                    context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position - 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                    MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.position - 1])
                    MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.position - 1])
                    MainActivity.instace!!.updateSong_Image(songBackGroundService.position - 1)
                    MainActivity.instace!!.updateVusikView(songBackGroundService.position - 1)
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                    MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                    MainActivity.instace!!.updateSeekBar()
                    MainActivity.instace!!.editor1.putInt("song_pos", songBackGroundService.position - 1)
                    MainActivity.instace!!.editor1.commit()
                    MainActivity.instace!!.listView.smoothScrollToPosition(songBackGroundService.position - 1)
                    MainActivity.instace!!.songsListAdopter!!.notifyDataSetChanged()
                }
                MainActivity.instace!!.updateSong_Total_Position(songBackGroundService.mediaPlayer.duration)
            }
        }
    }

    class NotificationPlayButtonHandler : BroadcastReceiver() {
        internal var view: View? = null

        override fun onReceive(context: Context, intent: Intent) {
            val remoteViews = RemoteViews(context.packageName, R.layout.notifactions)
            val binder = peekService(context, Intent(context, SongBackGroundService::class.java))
            if (binder != null) {
                val songBackGroundService = (binder as MyBinder).service
                if (songBackGroundService.mediaPlayer.isPlaying) {
                    remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
                    songBackGroundService.notification.contentView = remoteViews
                    songBackGroundService.startForeground(1337, songBackGroundService.notification)
                    songBackGroundService.mediaPlayer.pause()
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_play)

                } else {

                    songBackGroundService.mediaPlayer.start()
                    remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
                    songBackGroundService.notification.contentView = remoteViews
                    MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                    songBackGroundService.startForeground(1337, songBackGroundService.notification)
                    MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                    MainActivity.instace!!.updateSeekBar()
                }
            }
        }
    }


}
