package com.aust.karigor.wave;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created by Ahnaf on 7/24/2016.
 */
public class ServiceClass extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int SongPos;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle=("");
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;
    public void onCreate(){

        super.onCreate();
        player = new MediaPlayer();
        SongPos=0;

        initMusicPlayer();
        rand=new Random();
    }
    //shuffle toggle
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    //Method to pass the song list
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }


    public class MusicBinder extends Binder {
        ServiceClass getService() {
            return ServiceClass.this;
        }
    }


    //setting up Musicplayer
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //Listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playSong(){
        player.reset();
        //get song
        Song playSong = songs.get(SongPos);
        songTitle=playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        player.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

    }
    public void setSong(int songIndex){
        SongPos=songIndex;
    }

    //Media controls for user

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }


    public void playPrev(){
        SongPos--;
        if(SongPos<0) SongPos=songs.size()-1;
        playSong();
    }

    //skip to next

        public void playNext()
        {
            if(shuffle){
                int newSong = SongPos;
                while(newSong==SongPos){
                    newSong=rand.nextInt(songs.size());
                }
                SongPos=newSong;
            }
            else{
                SongPos++;
                if(SongPos >songs.size()) SongPos=0;
            }
            playSong();
        }


    @Override
    public void onDestroy() {
        stopForeground(true);
    }


}
