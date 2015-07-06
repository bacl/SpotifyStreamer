package com.baclpt.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bruno on 04-06-2015.
 */
public class TrackInfo implements Parcelable {
    private String spotifyArtistID;
    private String spotifyArtistName;
    private String trackName;
    private String albumName;
    private String albumArtLargeURL;
    private String albumArtSmallURL;
    private String previewURL;
    private String externalURL;

    public TrackInfo(String spotifyArtistID, String spotifyArtistName) {
        this.spotifyArtistID = spotifyArtistID;
        this.spotifyArtistName = spotifyArtistName;
    }

    public TrackInfo(Parcel in) {
        spotifyArtistID = in.readString();
        spotifyArtistName = in.readString();
        trackName = in.readString();
        albumName = in.readString();
        albumArtLargeURL = in.readString();
        albumArtSmallURL = in.readString();
        previewURL = in.readString();
        externalURL = in.readString();
    }

    public String getSpotifyArtistName() {
        return spotifyArtistName;
    }

    public void setSpotifyArtistName(String sotifyArtistName) {
        this.spotifyArtistName = sotifyArtistName;
    }
    public String getSpotifyArtistID() {
        return spotifyArtistID;
    }

    public void setSpotifyArtistID(String spotifyArtistID) {
        this.spotifyArtistID = spotifyArtistID;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumArtLargeURL() {
        return albumArtLargeURL;
    }

    public void setAlbumArtLargeURL(String albumArtLargeURL) {
        this.albumArtLargeURL = albumArtLargeURL;
    }

    public String getAlbumArtSmallURL() {
        return albumArtSmallURL;
    }

    public void setAlbumArtSmallURL(String albumArtSmallURL) {
        this.albumArtSmallURL = albumArtSmallURL;
    }

    public String getPreviewURL() {
        return previewURL;
    }

    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }
    public void setExternalURL(String externalURL) {
        this.externalURL = externalURL;
    }
    public String getExternalURL() {
        return externalURL;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(spotifyArtistID);
        dest.writeString(spotifyArtistName);
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(albumArtLargeURL);
        dest.writeString(albumArtSmallURL);
        dest.writeString(previewURL);
        dest.writeString(externalURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TrackInfo> CREATOR
            = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return new TrackInfo(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };


}
