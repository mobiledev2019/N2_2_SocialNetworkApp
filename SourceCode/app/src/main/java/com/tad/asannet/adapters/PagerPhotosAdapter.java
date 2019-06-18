package com.tad.asannet.adapters;

import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tad.asannet.R;
import com.tad.asannet.models.Images;
import com.bumptech.glide.Glide;
import com.tad.asannet.ui.views.AsannetImageView;

import java.util.List;

public class PagerPhotosAdapter extends PagerAdapter {


    private List<Images> IMAGES;
    private Context context;
    private LayoutInflater inflater;


    public PagerPhotosAdapter(Context context, List<Images> IMAGES) {
        this.context = context;
        this.IMAGES =IMAGES;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return IMAGES.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, final int position) {
        View imageLayout = inflater.inflate(R.layout.item_viewpager_image, view, false);

        assert imageLayout !=null;
        AsannetImageView imageView=imageLayout.findViewById(R.id.image);

        Glide.with(context)
                .load(IMAGES.get(position).getPath())
                .into(imageView);

        view.addView(imageLayout,0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
