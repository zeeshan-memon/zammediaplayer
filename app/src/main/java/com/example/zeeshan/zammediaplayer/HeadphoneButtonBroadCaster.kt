package com.example.zeeshan.zammediaplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import android.view.KeyEvent
import android.widget.RemoteViews
import android.widget.Toast

import java.util.ArrayList

/**
 * Created by zeeshan on 12/1/2017.
 */

class HeadphoneButtonBroadCaster : BroadcastReceiver() {
    private var paths: ArrayList<Uri>? = null
    private var song_Title: ArrayList<String>? = null
    private var song_Name: ArrayList<String>? = null
    internal lateinit var handler: Handler
    override fun onReceive(context: Context, intent: Intent) {

        val binder = peekService(context, Intent(context, SongBackGroundService::class.java))
        if (binder != null) {
            handler = Handler()
            val songBackGroundService = (binder as SongBackGroundService.MyBinder).service
            val remoteViews = RemoteViews(context.packageName, R.layout.notifactions)
            abortBroadcast()
            val key = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_KEY_EVENT) as KeyEvent
                    ?: return
            if (key.action == KeyEvent.ACTION_UP) {
                val keycode = key.keyCode
                if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    if (songBackGroundService.position == 0 && !songBackGroundService.mediaPlayer.isPlaying) {
                        paths = MainActivity.instace!!.paths
                        song_Name = MainActivity.instace!!.songs_Name
                        song_Title = MainActivity.instace!!.songs_Title
                        context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", paths).putExtra("name", song_Name).putExtra("title", song_Title))
                        MainActivity.instace!!.updateSong_name(song_Name!![0])
                        MainActivity.instace!!.updateSong_Artist(song_Title!![0])
                        MainActivity.instace!!.updateVusikView(0)
                        MainActivity.instace!!.updateSong_Image(0)
                        MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                        val runnable1 = Runnable {
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        }
                        handler.postDelayed(runnable1, 200)
                    } else {
                        if (songBackGroundService.position >= songBackGroundService.path!!.size - 1) {
                            context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                            MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![0])
                            MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![0])
                            MainActivity.instace!!.updateVusikView(0)
                            MainActivity.instace!!.updateSong_Image(0)
                            MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        } else {
                            context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position + 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                            MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.position + 1])
                            MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.position + 1])
                            MainActivity.instace!!.updateVusikView(songBackGroundService.position + 1)
                            MainActivity.instace!!.updateSong_Image(songBackGroundService.position + 1)
                            MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        }
                        MainActivity.instace!!.updateSong_Total_Position(songBackGroundService.mediaPlayer.duration)
                    }
                } else if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {

                    if (songBackGroundService.position == 0 && !songBackGroundService.mediaPlayer.isPlaying) {
                        paths = MainActivity.instace!!.paths
                        song_Name = MainActivity.instace!!.songs_Name
                        song_Title = MainActivity.instace!!.songs_Title
                        context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", song_Name!!.size - 1).putExtra("path", paths).putExtra("name", song_Name).putExtra("title", song_Title))
                        MainActivity.instace!!.updateSong_name(song_Name!![song_Name!!.size - 1])
                        MainActivity.instace!!.updateSong_Artist(song_Title!![song_Title!!.size - 1])
                        MainActivity.instace!!.updateVusikView(song_Name!!.size - 1)
                        MainActivity.instace!!.updateSong_Image(song_Name!!.size - 1)
                        MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                        val runnable1 = Runnable {
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        }
                        handler.postDelayed(runnable1, 200)

                    } else {
                        if (songBackGroundService.position <= 0) {
                            context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.song_Name!!.size - 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                            MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.song_Name!!.size - 1])
                            MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.song_Name!!.size - 1])
                            MainActivity.instace!!.updateVusikView(songBackGroundService.song_Name!!.size - 1)
                            MainActivity.instace!!.updateSong_Image(songBackGroundService.song_Name!!.size - 1)
                            MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        } else {
                            context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", songBackGroundService.position - 1).putExtra("path", songBackGroundService.path).putExtra("name", songBackGroundService.song_Name).putExtra("title", songBackGroundService.song_Title))
                            MainActivity.instace!!.updateSong_name(songBackGroundService.song_Name!![songBackGroundService.position - 1])
                            MainActivity.instace!!.updateSong_Artist(songBackGroundService.song_Title!![songBackGroundService.position - 1])
                            MainActivity.instace!!.updateSong_Image(songBackGroundService.position - 1)
                            MainActivity.instace!!.updateVusikView(songBackGroundService.position - 1)
                            MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                            MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                            MainActivity.instace!!.updateSeekBar()
                        }
                        MainActivity.instace!!.updateSong_Total_Position(songBackGroundService.mediaPlayer.duration)
                    }
                } else if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    if (!songBackGroundService.mediaPlayer.isPlaying) {
                        songBackGroundService.mediaPlayer.start()
                        remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
                        songBackGroundService.notification.contentView = remoteViews
                        MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                        songBackGroundService.startForeground(1337, songBackGroundService.notification)
                        MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                        MainActivity.instace!!.updateSeekBar()
                    } else {
                        if (songBackGroundService.mediaPlayer.isPlaying) {
                            remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
                            songBackGroundService.notification.contentView = remoteViews
                            songBackGroundService.startForeground(1337, songBackGroundService.notification)
                            songBackGroundService.mediaPlayer.pause()
                            MainActivity.instace!!.updateImageButton(R.drawable.ic_play)
                        }
                    }
                } else if (keycode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                    if (songBackGroundService.mediaPlayer.isPlaying) {
                        remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
                        songBackGroundService.notification.contentView = remoteViews
                        songBackGroundService.startForeground(1337, songBackGroundService.notification)
                        songBackGroundService.mediaPlayer.pause()
                        MainActivity.instace!!.updateImageButton(R.drawable.ic_play)
                    } else {
                        if (songBackGroundService.position == 0 && !songBackGroundService.mediaPlayer.isPlaying) {
                            paths = MainActivity.instace!!.paths
                            song_Name = MainActivity.instace!!.songs_Name
                            song_Title = MainActivity.instace!!.songs_Title

                            if (songBackGroundService.mediaPlayer.currentPosition > 0) {
                                songBackGroundService.mediaPlayer.start()
                                remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_pause)
                                songBackGroundService.notification.contentView = remoteViews
                                MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                                songBackGroundService.startForeground(1337, songBackGroundService.notification)
                                MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                                MainActivity.instace!!.updateSeekBar()
                            } else {
                                context.startService(Intent(context, SongBackGroundService::class.java).putExtra("pos", 0).putExtra("path", paths).putExtra("name", song_Name).putExtra("title", song_Title))
                                MainActivity.instace!!.updateImageButton(R.drawable.ic_pause)
                                MainActivity.instace!!.setMaxSeekBar(songBackGroundService.mediaPlayer.duration)
                                MainActivity.instace!!.updateSeekBar()
                            }
                        } else {
                            if (songBackGroundService.mediaPlayer.isPlaying) {
                                remoteViews.setImageViewResource(R.id.notification_Song_Play, R.drawable.ic_play)
                                songBackGroundService.notification.contentView = remoteViews
                                songBackGroundService.startForeground(1337, songBackGroundService.notification)
                                songBackGroundService.mediaPlayer.pause()
                                MainActivity.instace!!.updateImageButton(R.drawable.ic_play)
                            }
                        }
                    }
                }
            }

        }

    }
}
