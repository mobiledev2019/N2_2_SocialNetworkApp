package com.tad.asannet.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tad.asannet.R;
import com.tad.asannet.models.Users;
import com.tad.asannet.ui.activities.friends.SendActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by tad on 01/05/19.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<Users> usersList;
    private Context context;

    public UsersAdapter(List<Users> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
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

        holder.name.setText(usersList.get(position).getName());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        FirebaseFirestore.getInstance().collection("Users")
                .document(usersList.get(position).userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        try {
                            if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                    !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).userId)
                                        .update(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("friend_update", "success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("friend_update", "failure");
                                            }
                                        });

                                holder.name.setText(documentSnapshot.getString("name"));

                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);


                            } else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("name", documentSnapshot.getString("name"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).userId)
                                        .update(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("friend_update", "success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("friend_update", "failure");
                                            }
                                        });


                                holder.name.setText(documentSnapshot.getString("name"));

                            } else if (!documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                Map<String, Object> user = new HashMap<>();
                                user.put("image", documentSnapshot.getString("image"));

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("Friends")
                                        .document(usersList.get(holder.getAdapterPosition()).userId)
                                        .update(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i("friend_update", "success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("friend_update", "failure");
                                            }
                                        });


                                Glide.with(context)
                                        .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                        .load(documentSnapshot.getString("image"))
                                        .into(holder.image);

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });


        final String userid=usersList.get(position).userId;

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendActivity.startActivityfromAdapter(context,userid,usersList.get(holder.getAdapterPosition()).getName());
            }
        });


    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView image;
        private TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image = mView.findViewById(R.id.image);
            name = mView.findViewById(R.id.name);

        }
    }
}
