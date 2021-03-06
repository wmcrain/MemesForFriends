package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;


public class EditProfileActivity extends Activity{

    private static int RESULT_LOAD_IMAGE = 1;

    public static class BioPopup extends DialogFragment {

        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            final View rootView=inflater.inflate(R.layout.fragment_edit_bio_popup, container, false);

            final String biography = getArguments().getString("biography");
            final String contactinfo = getArguments().getString("phone");

            final EditText enterBio = (EditText) rootView.findViewById(R.id.bioPopupEditText);
            enterBio.setText(biography);

            Button cancel = (Button) rootView.findViewById(R.id.bioPopupCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterBio.setText(biography);
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
        setContentView(R.layout.activity_view_friend_profile);

        //TO-DO - get the bio and pic from the server instead of hardcoding in the xml

        final FragmentManager fm = getFragmentManager();
        final BioPopup popup = new BioPopup();
        final Bundle args = new Bundle();
        final Bundle contactargs = new Bundle();
        final TextView bio = (TextView) findViewById(R.id.viewProfileBioFriends);
        final TextView contactinfo = (TextView) findViewById(R.id.contactInfo);
       // final Switch request = (Switch) findViewById(R.id.requestFriend);
        final Button block = (Button) findViewById(R.id.blockUserFriends);
        final Button remove = (Button) findViewById(R.id.removeFriendFriends);
        block.setVisibility(View.GONE);
        remove.setVisibility(View.GONE);

        bio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String biography = bio.getText().toString();
                args.putString("biography", biography);
                popup.setArguments(args);
                popup.show(fm, "Edit Bio");

            }
        });

        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePicFriends);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        ServerRequest getProfile = ServerRequest.createGetProfileRequest(
                Preferences.getUserKey(this),
                Preferences.getUsername(this));

        final TextView nameView = (TextView) findViewById(R.id.name_ageFriends);

        getProfile.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                UserProfileData profile = responseMessage.getProfileArray(Keys.PROFILE).get(0);

                nameView.setText(profile.firstName+ " " + profile.lastName);

                bio.setText(profile.bio);

                contactinfo.setText(profile.phoneNumber);

                pic.setImageBitmap(profile.getPicture(EditProfileActivity.this));

            }
        });
    }

    public void setBio(String newBio){
        final TextView bio = (TextView) findViewById(R.id.viewProfileBioFriends);
        bio.setText(newBio);

        ServerRequest updateProfile = ServerRequest.createUpdateProfileRequest(
                Preferences.getUserKey(this), newBio, null);
        updateProfile.sendRequest(new ServerRequest.Callback() { });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView pic = (ImageView) findViewById(R.id.viewProfilePicFriends);
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            bitmap = Bitmap.createScaledBitmap(bitmap, pic.getWidth(), pic.getHeight(), true);

            ServerRequest updateProfile = ServerRequest.createUpdateProfileRequest(
                    Preferences.getUserKey(this), null, bitmap);
            updateProfile.sendRequest(new ServerRequest.Callback() { });

            pic.setImageBitmap(bitmap);

        }


    }
}
