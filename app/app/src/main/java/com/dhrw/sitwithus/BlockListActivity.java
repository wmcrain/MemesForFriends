package com.dhrw.sitwithus;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.dhrw.sitwithus.server.BlockedUserData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import android.widget.Button;

public class BlockListActivity extends Activity {

    private List<BlockedUserData> blockedUsers = new ArrayList<>();
    private BlockArrayAdapter adapter;

    private class BlockArrayAdapter extends ArrayAdapter {

        BlockArrayAdapter() {
            super(BlockListActivity.this, R.layout.activity_block_list);
        }

        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final BlockedUserData blockedUser = blockedUsers.get(position);
            View view = convertView == null ? LayoutInflater.from(BlockListActivity.this)
                    .inflate(R.layout.activity_block_entry, null) : convertView;

            // Set the name of the user in the block list
            TextView name = (TextView) view.findViewById(R.id.blockEntryName);
            name.setText(blockedUser.firstName + " " + blockedUser.lastName);

            // Set a click listener for the unblocking of a user
            Button delete = (Button) view.findViewById(R.id.blockEntryRemove);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Remove the user that has just been unblocked
                    blockedUsers.remove(position);
                    adapter.notifyDataSetChanged();

                    // Tell the server that the user has been unblocked
                    ServerRequest.createUnblockRequest(
                            Preferences.getUserKey(BlockListActivity.this),
                            blockedUser.userKey).sendRequest();
                }
            });

            return view;
        }

        public int getCount(){
            return blockedUsers.size();
        }
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View empty = findViewById(R.id.blockEmpty);
        ListView list = (ListView) findViewById(R.id.block_list);
        list.setEmptyView(empty);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);

        // Set the adapter for the block list
        adapter = new BlockArrayAdapter();
        ((ListView) findViewById(R.id.block_list)).setAdapter(adapter);

        // Populate the block list with the list of users that this user has blocked from the server
        ServerRequest.createGetBlockListRequest(Preferences.getUserKey(this)).sendRequest(
                new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);
                        blockedUsers = responseMessage.getBlockedUserList(Keys.BLOCKED);
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }
}
