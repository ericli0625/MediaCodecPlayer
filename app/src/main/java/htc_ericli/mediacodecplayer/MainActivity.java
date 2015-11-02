package htc_ericli.mediacodecplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private final static String TAG = MainActivity.class.getName();

    private SurfaceView mSurfaceView;

    private VideoDecoderThread mVideoDecoder;
    private AudioDecoderThread mAudioDecoder;

    public static Uri filePathUri;
    private final static String filePath = "/sdcard/Movies/demo.mp4";
    public MediaExtractor extractor;

    private String [] mineArr = new String[2];
    private MediaFormat [] formatArr = new MediaFormat[2];

    private int numTracks = 0;

    private ImageView mImageView = null;
    private Button mButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Retriever();
            }
        });

        mSurfaceView.getHolder().addCallback(this);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.Play:
                Play();
                break;
            case R.id.Stop:
                Stop();
                break;
            case R.id.Pause:
                Pause();
                break;
            case R.id.Retriever:
                Retriever();
                break;
            default:
                break;
        }

        return true;
    }

    private void Stop(){
        mVideoDecoder.setRun_Thread(false);
        mVideoDecoder = null;
    }

    private void Retriever(){

        Intent intent=new Intent(MainActivity.this,RetrieveService.class);
        intent.putExtra("path", filePath);
        startService(intent);

    }

    private void Pause(){

    }

    private void Play(){

        mVideoDecoder = new VideoDecoderThread();
        mAudioDecoder = new AudioDecoderThread();

        if (mVideoDecoder != null) {

            ExtractorFile();

            for (int i=0; i<numTracks; i++) {

                if (mineArr[i].startsWith("video/")) {
                    mVideoDecoder.MediaCodecConfigure(mSurfaceView.getHolder().getSurface(), extractor, mineArr, formatArr,i);
                    mVideoDecoder.start();
                }
/*
                if (mineArr[i].startsWith("audio/")){
                    mAudioDecoder.MediaCodecConfigure(holder.getSurface(), extractor, mineArr, formatArr,i);
                    mAudioDecoder.start();
                }
*/
            }

        }else{
            mVideoDecoder=null;
            mAudioDecoder=null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void ExtractorFile(){

        extractor = new MediaExtractor();

        filePathUri = Uri.parse(filePath);

        try {
            extractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        numTracks = extractor.getTrackCount();

        for (int i=0; i<numTracks; i++){
            MediaFormat format = extractor.getTrackFormat(i);
            String mine = format.getString(MediaFormat.KEY_MIME);

            mineArr[i]=mine;
            formatArr[i]=format;

            Log.d(TAG, "MediaFormat[" + i + "]=" + mineArr[i]);
            Log.d(TAG, "MediaFormat1[" + i + "]=" + formatArr[i]);

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoDecoder.setRun_Thread(false);
        mVideoDecoder = null;
    }

}
