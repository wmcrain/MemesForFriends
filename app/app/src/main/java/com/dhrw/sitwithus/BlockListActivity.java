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

import com.dhrw.sitwithus.data.Profile;

import org.w3c.dom.Text;

public class BlockListActivity extends Activity {

    List<Profile> blocked;
    String[] testArray = {"Hazem", "Alex", "Ryan", "David","Will","Abdul","Casey","Sara","Josh"};

    private class BlockArrayAdapter extends ArrayAdapter {

        public BlockArrayAdapter() {
            super(BlockListActivity.this, R.layout.activity_block_list);
        }

        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Profile profile = blocked.get(position);

            View view = LayoutInflater.from(BlockListActivity.this).inflate(R.layout.activity_block_entry, null);
            TextView name = (TextView) findViewById(R.id.block_entry_name);
            name.setText(profile.firstName + " " + profile.lastName);

            ImageView pic = (ImageView) findViewById(R.id.block_entry_picture);
            if (profile.picture != null)
                pic.setImageBitmap(profile.picture);
            else
                pic.setImageResource(R.mipmap.david);

            Button delete = (Button) findViewById(R.id.);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });


            return view;
        }
        public int getCount(){
            return blocked.size();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);

        blocked = new ArrayList<>();

        final ListView listView = (ListView) findViewById(R.layout.activity_block_list);


        final BlockArrayAdapter adapter = new BlockArrayAdapter();
        listView.setAdapter(adapter);


    }
}
