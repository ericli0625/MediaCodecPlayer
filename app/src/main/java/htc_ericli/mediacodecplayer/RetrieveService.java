package htc_ericli.mediacodecplayer;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RetrieveService extends IntentService {

    private final static String TAG = MainActivity.class.getName();

    private String filePath = null;

    private MediaMetadataRetriever mmr;

    public RetrieveService() {
        super("RetrieveService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("IntentService onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        filePath = intent.getStringExtra("path");
        Log.v(TAG, "filePath = "+filePath);

        mmr = new MediaMetadataRetriever();

        try{
            if (filePath != null) {
                mmr.setDataSource(filePath);
            } else {
                Log.v(TAG, "filePath == null");
            }
        } catch (IllegalArgumentException ex){
            Log.e(TAG,"Retriever IllegalArgumentException");
        }

        for (int i=1;i<=30;i++){

            Bitmap bmp = mmr.getFrameAtTime(1000*1000*i, MediaMetadataRetriever.OPTION_CLOSEST);

            if (null != bmp) {
                Log.d(TAG, "setImageBitmap is success.");
                storeImage(bmp);
                //mImageView.setImageBitmap(bmp);
            }else{
                Log.d(TAG,"setImageBitmap is fail.");
            }

        }

        mmr.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("IntentService onDestroy");
    }

    private void storeImage(Bitmap image) {
        Log.d(TAG,"storeImage 1.");
        File pictureFile = getOutputMediaFile();
        Log.d(TAG,"storeImage 2.");
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            Log.d(TAG,"storeImage 3.");
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            Log.d(TAG, "storeImage 4.");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File("/sdcard/"+ Environment.DIRECTORY_DCIM);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

}
