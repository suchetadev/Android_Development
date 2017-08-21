package com.example.patil.tabtest;

/**
 * Created by patil on 5/19/2017.
 */

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


public class TabFragment1 extends Fragment implements View.OnClickListener{

    private static final String CLOUD_VISION_API_KEY = "REPLACEHEREWITHYOURCLOUDVISIONKEY";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private ImageView mMainImage;

    private TextView mTextView;
    private TextView mTextView4;

    private Button ButtonFav;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("SUCHETA", " inside onCreateView tabFragment 1 ... ");

        View v = inflater.inflate(R.layout.tab_fragment_1, container, false);

        ImageButton imageButton2 = (ImageButton) v.findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener(this);

        ImageButton imageButton3 = (ImageButton) v.findViewById(R.id.imageButton3);
        imageButton3.setOnClickListener(this);

        ButtonFav = (Button) v.findViewById(R.id.buttonFav);
        ButtonFav.setOnClickListener(this);

        mImageDetails = (TextView) v.findViewById(R.id.image_details);
        mMainImage = (ImageView) v.findViewById(R.id.main_image);

        mTextView = (TextView) v.findViewById(R.id.textView);
        mTextView4 = (TextView) v.findViewById(R.id.textView4);

        ButtonFav.setVisibility(View.GONE);

       // DBHandler db = new DBHandler(getActivity());
       // db.deleteAllItems();

        return v;

    }

    @Override
    public void onClick(View v) {
        Log.d("SUCHETA", " inside onClick ... ");
        switch (v.getId()) {
            case R.id.imageButton2:
                Log.d("SUCHETA", " inside onClick [camera] ... ");
                startCamera();
                break;
            case R.id.imageButton3:
                Log.d("SUCHETA", " inside onClick [gallery] ... ");
                startGalleryChooser();
                break;
            case R.id.buttonFav:
                Log.d("SUCHETA", " inside onClick [buttonFav] ... ");
                saveToFav();
                break;
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                getActivity(),
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(getActivity(), GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    public void uploadImage(Uri uri) {
        Log.d("SUCHETA", " inside uploadImage ... ");

        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                1200);


                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);
                ButtonFav.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }

    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        mTextView.setText("");
        mTextView4.setText("");


        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {

                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getActivity().getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getActivity().getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();


                    //code to add information about closet item to database
                    ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream1);
                    byte[] imageBytes1 = byteArrayOutputStream1.toByteArray();

                    String str = convertResponseToString(response);

                    String type = calculateType(response);
                    //str += "type calculated : " + type + "\n";

                    String color = calculateColor(response);
                    //str += "color calculated : " + color + "\n";

                    saveResponseToDatabase(response, imageBytes1, type,color);

                    return str;


                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                mImageDetails.setText(result);
                mTextView.setText("");
                mTextView4.setText("");

            }
        }.execute();
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "Image features extracted \n";

        List<EntityAnnotation> labels_original = response.getResponses().get(0).getLabelAnnotations();

        List<EntityAnnotation> labels = labels_original.subList(0,5);

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }

    public String calculateType(BatchAnnotateImagesResponse response) {

        String type = "misc";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        ArrayList<String> tops = new ArrayList<String>();
        tops.add("sleeve"); tops.add("top"); tops.add("tops");
        tops.add("t shirt"); tops.add("shirt"); tops.add("sleeves");

        ArrayList<String> bottoms = new ArrayList<String>();
        bottoms.add("jeans"); bottoms.add("skirt"); bottoms.add("trousers");
        bottoms.add("bottoms"); bottoms.add("jean"); bottoms.add("shorts");

        ArrayList<String> shoes = new ArrayList<String>();
        shoes.add("shoes"); shoes.add("shoe"); shoes.add("trousers");
        shoes.add("footwear"); shoes.add("footwears"); shoes.add("trouser");

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                //message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
                //message += "\n";
                String temptype = label.getDescription();

                if (tops.contains(temptype)) {
                    type = "tops";
                    return  type;
                } else if (bottoms.contains(temptype)){
                    type = "bottoms";
                    return  type;
                } else if (shoes.contains(temptype)) {
                    type = "shoes";
                    return  type;
                }
            }
        } else {
            //message += "nothing";
        }

        return  type;
    }

    public String calculateColor(BatchAnnotateImagesResponse response) {

        String color = "misc";

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        ArrayList<String> red = new ArrayList<String>();
        red.add("red"); red.add("pink");

        ArrayList<String> blue = new ArrayList<String>();
        blue.add("blue"); blue.add("blues"); blue.add("navy blue");

        ArrayList<String> green = new ArrayList<String>();
        green.add("green");

        if (labels != null) {
            for (EntityAnnotation label : labels) {
              //  message += String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription());
              //  message += "\n";
                String temptype = label.getDescription();

                if (red.contains(temptype)) {
                    color = "red";
                    return  color;
                } else if (blue.contains(temptype)){
                    color = "blue";
                    return  color;
                } else if (green.contains(temptype)) {
                    color = "green";
                    return color;
                }
            }
        } else {
            // message += "nothing";
        }

        return  color;
    }


    private void saveResponseToDatabase(BatchAnnotateImagesResponse response, byte[] image, String type, String color) {

        Log.d("DATABASE", " ********** image***************Inside saveResponseToDatabase function ... ");

        DBHandler db = new DBHandler(getActivity());

        // Inserting Closet/Rows
        Log.d("Insert: ", "Inserting ..");

        db.addClosetItem(new ClosetItem(type, color, image, 0));

        Log.d("Insert: ", "Inserting done ..");

    }

    private void saveToFav() {

        Log.d("DATABASE", " ********** image***************Inside saveToFav function ... ");

        DBHandler db = new DBHandler(getActivity());

        db.updateFavorite();

        Toast.makeText(getActivity().getApplicationContext(),"Marked as Favorite",Toast.LENGTH_LONG).show();
    }

}
