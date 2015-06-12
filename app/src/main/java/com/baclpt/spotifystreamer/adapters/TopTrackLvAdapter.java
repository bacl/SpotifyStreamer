package com.baclpt.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baclpt.spotifystreamer.R;
import com.baclpt.spotifystreamer.data.TrackInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * List view adapter to display a track info
 *
 * Created by Bruno on 04-06-2015.
 */
public class TopTrackLvAdapter extends ArrayAdapter<TrackInfo> {


    public TopTrackLvAdapter(Context context, int resource, ArrayList<TrackInfo> artists) {
        super(context, resource, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        TrackInfo artistInfo = getItem(position);

        // inflate the view if isn't being reused
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //  set track and album name
        viewHolder.mTrackName.setText(artistInfo.getTrackName());
        viewHolder.mAlbumName.setText(artistInfo.getAlbumName());

        //  set artist thumbnail
        if (artistInfo.getAlbumArtSmallURL() != null) {
            Picasso.with(getContext()).load(artistInfo.getAlbumArtSmallURL()).into(viewHolder.mThumbnail);
        } else {
            // if doesn't have a thumbnail sets a default one
            viewHolder.mThumbnail.setImageResource(R.mipmap.thumbnail_fail);
        }

        return convertView;
    }


    /**
     * Cache View Holder
     */
    protected class ViewHolder {
        public ImageView mThumbnail;
        public TextView mTrackName;
        public TextView mAlbumName;

        public ViewHolder(View view) {
            mThumbnail = (ImageView) view.findViewById(R.id.list_item_abum_thumb_imageView);
            mAlbumName = (TextView) view.findViewById(R.id.list_item_abum_name_textView);
            mTrackName = (TextView) view.findViewById(R.id.list_item_track_name_textView);
        }
    }

}
