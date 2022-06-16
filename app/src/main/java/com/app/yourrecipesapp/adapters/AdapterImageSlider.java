package com.app.yourrecipesapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.app.yourrecipesapp.R;
import com.app.yourrecipesapp.config.AppConfig;
import com.app.yourrecipesapp.databases.prefs.SharedPref;
import com.app.yourrecipesapp.models.Images;
import com.app.yourrecipesapp.utils.Constant;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterImageSlider extends PagerAdapter {

    private Context context;
    private List<Images> items;
    private OnItemClickListener onItemClickListener;
    private SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Images images, int position);
    }

    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public AdapterImageSlider(Context context, List<Images> list) {
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.items = list;
    }

    @NonNull
    public Object instantiateItem(ViewGroup viewGroup, final int position) {
        final Images post = items.get(position);
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_image_slider, viewGroup, false);

        ImageView news_image = inflate.findViewById(R.id.image_detail);

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMAGE_FRONT + post.video_id + Constant.YOUTUBE_IMAGE_BACK_MQ)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);
        } else if (post.content_type != null && post.content_type.equals("Url")) {
            Picasso.get()
                    .load(sharedPref.getApiUrl() + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.get()
                    .load(sharedPref.getApiUrl() + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);
        } else {
            Picasso.get()
                    .load(sharedPref.getApiUrl() + "/upload/" + post.image_name.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);
        }

        if (AppConfig.ENABLE_RTL_MODE) {
            news_image.setRotationY(180);
        }

        viewGroup.addView(inflate);
        return inflate;
    }

    public int getCount() {
        return this.items.size();
    }

    public void destroyItem(ViewGroup viewGroup, int i, @NonNull Object obj) {
        viewGroup.removeView((View) obj);
    }

}