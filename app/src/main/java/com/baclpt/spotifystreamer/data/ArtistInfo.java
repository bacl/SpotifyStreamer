package com.baclpt.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bruno on 04-06-2015.
 */
public class ArtistInfo implements Parcelable {
    private String spotifyId;
    private String name;
    private String imageURL;

    public ArtistInfo() {
    }

    public ArtistInfo(String spotifyId, String name, String imageURL) {
        this.spotifyId = spotifyId;
        this.name=name;
        this.imageURL=imageURL;
    }

    public ArtistInfo(Parcel parcel) {
        spotifyId = parcel.readString();
        name = parcel.readString();
        imageURL = parcel.readString();
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(spotifyId);
        dest.writeString(name);
        dest.writeString(imageURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ArtistInfo> CREATOR
            = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
