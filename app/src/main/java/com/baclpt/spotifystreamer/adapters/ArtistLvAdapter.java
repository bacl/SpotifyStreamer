package com.baclpt.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baclpt.spotifystreamer.R;
import com.baclpt.spotifystreamer.data.ArtistInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 *  List view adapter to display artist info
 *
 * Created by Bruno on 04-06-2015.
 */
public class ArtistLvAdapter extends ArrayAdapter<ArtistInfo> {


    public ArtistLvAdapter(Context context, int resource, ArrayList<ArtistInfo> artists) {
        super(context, resource, artists);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ArtistInfo artistInfo = getItem(position);

        // inflate the view if isn't being reused
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //  set artist name
        viewHolder.mName.setText(artistInfo.getName());

        //  set artist thumbnail
        if (artistInfo.getImageURL() != null) {
            Picasso.with(getContext()).load(artistInfo.getImageURL()).into( viewHolder.mThumbnail);
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
        public final ImageView mThumbnail;
        public final TextView mName;

        public ViewHolder(View view) {
            mThumbnail = (ImageView) view.findViewById(R.id.list_item_thumb_imageView);
            mName = (TextView) view.findViewById(R.id.list_item_name_textView);
        }
    }

}
