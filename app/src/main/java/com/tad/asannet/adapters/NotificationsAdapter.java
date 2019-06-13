package com.tad.asannet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tad.asannet.R;
import com.tad.asannet.models.Notification;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tad on 01/05/19.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private List<Notification> notificationsList;
    private Context context;

    public NotificationsAdapter(List<Notification> notificationsList, Context context) {
        this.notificationsList = notificationsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification,parent,false);
        return new ViewHolder(view);
    }

	@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
	
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Notification notification=notificationsList.get(position);

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(notification.getImage())
                .into(holder.image);

        holder.title.setText(notification.getTitle());
        holder.body.setText(notification.getBody());

        if(notification.getBody().toLowerCase().equals("liked your post")){
            holder.type_image.setImageResource(R.drawable.ic_favorite_red_24dp);
        }else if(notification.getBody().toLowerCase().equals("commented on your post")){
            holder.type_image.setImageResource(R.drawable.ic_comment_blue);
        }else if(notification.getBody().toLowerCase().equals("sent you friend request")){
            holder.type_image.setImageResource(R.drawable.ic_person_add_yellow_24dp);
        }else if(notification.getBody().toLowerCase().equals("accepted your friend request")){
            holder.type_image.setImageResource(R.drawable.ic_person_green_24dp);
        }

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView image;
        private ImageView type_image;
        private TextView title,body;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            type_image = mView.findViewById(R.id.type_image);
            title = mView.findViewById(R.id.title);
            body = mView.findViewById(R.id.body);

        }
    }
}
