package com.dhrw.sitwithus;

/**
 * Created by Hazem on 11/9/17.
 */
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FriendListActivity extends Activity {

    String[] testArray = {"Hazem", "Alex", "Ryan", "David","Will","Abdul","Casey","Sara","Josh"};
    private class FriendArrayAdapter extends ArrayAdapter {

        public FriendArrayAdapter() {
            super(FriendListActivity.this, R.layout.activity_friend_entry);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = LayoutInflater.from(FriendListActivity.this).inflate(R.layout.activity_friend_entry, null);
            TextView name = (TextView) view.findViewById(R.id.friend_entry_name);
            ImageView pic = (ImageView) view.findViewById(R.id.friend_entry_picture);
            pic.setImageResource(R.mipmap.david);
            name.setText(testArray[position]);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //
                }
            });

            //View view = new View()
            return view;
        }

        @Override
        public int getCount() {
            return testArray.length;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_friend_list);

        ListView listView = (ListView) findViewById(R.id.friened_list);
        listView.setAdapter(new FriendArrayAdapter());
    }
}
