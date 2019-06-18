package com.tad.asannet.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tad.asannet.R;
import com.tad.asannet.adapters.addFriends.AddFriendAdapter;
import com.tad.asannet.adapters.addFriends.RecyclerViewTouchHelper;
import com.tad.asannet.models.Friends;
import com.tad.asannet.models.ViewFriends;
import com.tad.asannet.ui.activities.friends.SearchUsersActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by tad on 29/3/18.
 */

public class AddFriends extends Fragment {

    View mView;
    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.friends_add_frag, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = mView.findViewById(R.id.recyclerView);
        refreshLayout=mView.findViewById(R.id.refreshLayout);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerViewTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof AddFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(mView.findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            }
        });

        usersList = new ArrayList<>();
        usersAdapter = new AddFriendAdapter(usersList, view.getContext(), mView.findViewById(R.id.layout));

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(usersAdapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllUsers();
            }
        });

        getAllUsers();

    }

    public void getAllUsers() {
        usersList.clear();
        usersAdapter.notifyDataSetChanged();
        mView.findViewById(R.id.default_item).setVisibility(View.GONE);
        refreshLayout.setRefreshing(true);

        firestore.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if (!doc.getDocument().getId().equals(mAuth.getCurrentUser().getUid())) {
                                        Friends friends = doc.getDocument().toObject(Friends.class).withId(doc.getDocument().getString("id"));
                                        usersList.add(friends);
                                        usersAdapter.notifyDataSetChanged();
                                        refreshLayout.setRefreshing(false);
                                    }

                                }
                            }

                            if(usersList.isEmpty()){
                                refreshLayout.setRefreshing(false);
                                mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                            }

                        }else{
                            refreshLayout.setRefreshing(false);
                            mView.findViewById(R.id.default_item).setVisibility(View.VISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        refreshLayout.setRefreshing(false);
                        Toast.makeText(mView.getContext(), "Some technical error occurred", Toast.LENGTH_SHORT).show();
                        Log.w("Error", "listen:error", e);

                    }
                });



    }


}
