package com.baclpt.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bruno on 04-06-2015.
 */
public class TrackInfo implements Parcelable {
    private String sotifyArtistID;
    private String trackName;
    private String albumName;
    private String albumArtLargeURL;
    private String albumArtSmallURL;
    private String previewURL;

    public TrackInfo(String sotifyArtistID) {
        this.sotifyArtistID = sotifyArtistID;
    }

    public TrackInfo(Parcel in) {
        sotifyArtistID = in.readString();
        trackName = in.readString();
        albumName = in.readString();
        albumArtLargeURL = in.readString();
        albumArtSmallURL = in.readString();
        previewURL = in.readString();
    }

    public String getSotifyArtistID() {
        return sotifyArtistID;
    }

    public void setSotifyArtistID(String sotifyArtistID) {
        this.sotifyArtistID = sotifyArtistID;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sotifyArtistID);
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(albumArtLargeURL);
        dest.writeString(albumArtSmallURL);
        dest.writeString(previewURL);
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
