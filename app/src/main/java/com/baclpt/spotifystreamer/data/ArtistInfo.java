package com.baclpt.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Bruno on 04-06-2015.
 */
public class ArtistInfo implements Parcelable {
    private String sotifyId;
    private String name;
    private String imageURL;

    public ArtistInfo() {
    }


    public ArtistInfo(Parcel parcel) {
        sotifyId = parcel.readString();
        name = parcel.readString();
        imageURL = parcel.readString();
    }

    public String getSotifyId() {
        return sotifyId;
    }

    public void setSotifyId(String sotifyId) {
        this.sotifyId = sotifyId;
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
        dest.writeString(sotifyId);
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
