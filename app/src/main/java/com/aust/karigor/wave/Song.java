package com.aust.karigor.wave;

/**
 * Created by Ahnaf on 7/24/2016.
 */
public class Song {
    //track informations
    private long id;
    private String title;
    private String artist;
    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}

}
