package com.example.patil.tabtest;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patil on 5/19/2017.
 */

public class TabFragment2 extends Fragment implements View.OnClickListener {


    LinearLayout mLinearLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("SUCHETA", " inside onCreateView tabFragment 2 ... ");

        View v = inflater.inflate(R.layout.tab_fragment_2, container, false);

       ScrollView v1 = (ScrollView) v.findViewById(R.id.scrollView);
       mLinearLayout = (LinearLayout)v.findViewById(R.id.linear);



        // Get set of images
        List<ClosetItem> arrClosetItems = new ArrayList<ClosetItem>();

        arrClosetItems = getClosetItems();
        int count = arrClosetItems.size();

        // show default images

        for(int x=0;x<count;x++) {
            ImageView image = new ImageView(getActivity());
            ClosetItem tempItem = arrClosetItems.get(x);
            byte[] imageDB = tempItem.getImageSource();
            image.setImageBitmap(BitmapFactory.decodeByteArray(imageDB,0,imageDB.length));
            mLinearLayout.addView(image);
        }

        Button ButtonTop = (Button) v.findViewById(R.id.buttonTop);
        ButtonTop.setOnClickListener(this);


        Button ButtonBottom = (Button) v.findViewById(R.id.buttonBottom);
        ButtonBottom.setOnClickListener(this);

        Button ButtonShoes = (Button) v.findViewById(R.id.buttonShoes);
        ButtonShoes.setOnClickListener(this);

        Button ButtonRed = (Button) v.findViewById(R.id.buttonRed);
        ButtonRed.setOnClickListener(this);

        Button ButtonGreen = (Button) v.findViewById(R.id.buttonGreen);
        ButtonGreen.setOnClickListener(this);

        Button ButtonBlue = (Button) v.findViewById(R.id.buttonBlue);
        ButtonBlue.setOnClickListener(this);

        Button ButtonFavList = (Button)v.findViewById(R.id.buttonFavList);
        ButtonFavList.setOnClickListener(this);

        return v;

    }

   @Override
    public void onClick(View v) {

       Log.d("SUCHETA", " inside onClick ... ");
        switch (v.getId()) {
            case R.id.buttonTop:
                Log.d("SUCHETA", " inside onClick [buttonTop] ... ");
                loadImagesBasedOnTypes(v, "tops");
                break;
            case R.id.buttonBottom:
                Log.d("SUCHETA", " inside onClick [buttonBottom] ... ");
                loadImagesBasedOnTypes(v, "bottoms");
                break;
            case R.id.buttonShoes:
                Log.d("SUCHETA", " inside onClick [buttonShoes] ... ");
                loadImagesBasedOnTypes(v, "shoes");
                break;
            case R.id.buttonRed:
                Log.d("SUCHETA", " inside onClick [buttonRed] ... ");
                loadImagesBasedOnColors(v, "red");
                break;
            case R.id.buttonGreen:
                Log.d("SUCHETA", " inside onClick [buttonGreen] ... ");
                loadImagesBasedOnColors(v, "green");
                break;
            case R.id.buttonBlue:
                Log.d("SUCHETA", " inside onClick [buttonBlue] ... ");
                loadImagesBasedOnColors(v, "blue");
                break;
            case R.id.buttonFavList:
                Log.d("SUCHETA"," inside onClick [buttonFavList] ... ");
                loadImagesBasedOnFavorite(v);
                break;
        }
    }

     //based on type
    public void loadImagesBasedOnTypes(View v, String category) {

        mLinearLayout.removeAllViewsInLayout();

        // Get set of images
        List<ClosetItem> arrClosetItems = new ArrayList<ClosetItem>();

        arrClosetItems = getClosetItems();
        int count = arrClosetItems.size();

        Log.d("SUCHETA", " loadimagesbased on types count : " + count);

        // show default images
        for(int x=0;x<count;x++) {
            ImageView image = new ImageView(getActivity());

            ClosetItem tempItem = arrClosetItems.get(x);
            String type = tempItem.getType();
            if (type.equals(category)) {
                byte[] imageDB = tempItem.getImageSource();
                image.setImageBitmap(BitmapFactory.decodeByteArray(imageDB, 0, imageDB.length));
                mLinearLayout.addView(image);
            }
        }

    }

    //based on color
    public void loadImagesBasedOnColors(View v, String color) {

        mLinearLayout.removeAllViewsInLayout();

        // Get set of images
        List<ClosetItem> arrClosetItems = new ArrayList<ClosetItem>();

        arrClosetItems = getClosetItems();
        int count = arrClosetItems.size();

        Log.d("SUCHETA", " loadimagesbased on types count : " + count);

        // show default images
        for(int x=0;x<count;x++) {
            ImageView image = new ImageView(getActivity());
            ClosetItem tempItem = arrClosetItems.get(x);
            String type = tempItem.getColor();
            if (type.equals(color)) {
                byte[] imageDB = tempItem.getImageSource();
                image.setImageBitmap(BitmapFactory.decodeByteArray(imageDB, 0, imageDB.length));
                mLinearLayout.addView(image);
            }
        }

    }


    //based on favorite items
    public void loadImagesBasedOnFavorite(View v ) {

        mLinearLayout.removeAllViewsInLayout();

        // Get set of images
        List<ClosetItem> arrClosetItems = new ArrayList<ClosetItem>();

        arrClosetItems = getClosetItems();
        int count = arrClosetItems.size();


        // show images
        for(int x=0;x<count;x++) {
            ImageView image = new ImageView(getActivity());
            ClosetItem tempItem = arrClosetItems.get(x);
            int fav= tempItem.getFav();
            if (fav == 1) {
                byte[] imageDB = tempItem.getImageSource();
                image.setImageBitmap(BitmapFactory.decodeByteArray(imageDB, 0, imageDB.length));
                mLinearLayout.addView(image);
            }
        }

    }



    //get closet items from database
    public List<ClosetItem> getClosetItems() {
        DBHandler db = new DBHandler(getActivity());

        List<ClosetItem> closetItemsList = db.getAllItems();

        db.close();

        return closetItemsList;
    }
}
