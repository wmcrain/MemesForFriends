package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.TextView;

import java.net.URI;


public class EditProfileActivity extends Activity{

    public static class BioPopup extends DialogFragment {

        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            final View rootView=inflater.inflate(R.layout.fragment_edit_bio_popup, container, false);

            final String biography = getArguments().getString("biography");

            final EditText enterBio = (EditText) rootView.findViewById(R.id.bioPopupEditText);
            enterBio.setText(biography);

            Button cancel = (Button) rootView.findViewById(R.id.bioPopupCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            Button confirm = (Button) rootView.findViewById(R.id.bioPopupConfirm);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newBio = enterBio.getText().toString();
                    ((EditProfileActivity)getActivity()).setBio(newBio);
                    dismiss();
                }
            });

            return rootView;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        final FragmentManager fm = getFragmentManager();
        final BioPopup popup = new BioPopup();
        final Bundle args = new Bundle();

        final TextView bio = (TextView) findViewById(R.id.viewProfileBio);

        bio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String biography = bio.getText().toString();
                args.putString("biography", biography);
                popup.setArguments(args);
                popup.show(fm, "Edit Bio");

            }
        });

        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePic);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //edit the pic here
            }
        });

    }

    public void setBio(String newBio){
        final TextView bio = (TextView) findViewById(R.id.viewProfileBio);
        bio.setText(newBio);
    }

    public void setImage(URI newImage){
        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePic);

    }

}
