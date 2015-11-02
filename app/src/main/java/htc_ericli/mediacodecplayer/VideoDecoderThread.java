package htc_ericli.mediacodecplayer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecoderThread  extends Thread{

    private static final String TAG = MainActivity.class.getName();
    public long timeoutUs = 0;

    public MediaExtractor extractor;
    public MediaCodec codec;

    private boolean RUN_THREAD = true;

    public void MediaCodecConfigure(Surface surface,MediaExtractor myExtractor,String[] mine,MediaFormat[] format,int i){

        extractor = myExtractor;
        extractor.selectTrack(i);

        try {
            codec = MediaCodec.createDecoderByType(mine[i]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        codec.configure(format[i],surface,null,0);
        codec.start();


    }

    public void initialMediaCodec(){

        boolean isEOS = false;

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        while (RUN_THREAD) {

            if (!isEOS) {
                int inputBufferIndex = codec.dequeueInputBuffer(timeoutUs);
                if (inputBufferIndex >= 0) {
                    // if API level >= 21, get input buffer here
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                    // fill inputBuffers[inputBufferIndex] with valid data

                    int sampleSize = extractor.readSampleData(inputBuffer, 0 /* offset */);
                    long presentationTimeUs = 0;

                    if (sampleSize < 0) {
                        Log.d(TAG, "saw input EOS.");
                        isEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                    }

                    codec.queueInputBuffer(
                            inputBufferIndex,
                            0 /* offset */,
                            sampleSize,
                            presentationTimeUs,
                            isEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                    if (!isEOS) {
                        extractor.advance();
                    }

                }
            }


            int outputBufferIndex = codec.dequeueOutputBuffer(info,timeoutUs);
            if (outputBufferIndex >= 0) {
                // if API level >= 21, get output buffer here
                ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                // outputBuffer is ready to be processed or rendered.

                if (outputBuffer != null) {
                    codec.releaseOutputBuffer(outputBufferIndex, true);
                }

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // can ignore if API level >= 21 and using getOutputFormat(outputBufferIndex)
                MediaFormat format = codec.getOutputFormat();
                Log.d(TAG, "output format has changed to " + format);
            }else {
                Log.d(TAG, "dequeueOutputBuffer returned " + outputBufferIndex);
            }

        }

        codec.stop();
        codec.release();
        codec = null;

    }

    public void setRun_Thread(boolean value){

        RUN_THREAD = value;

        if (!RUN_THREAD){
            this.interrupt();
        }

    }

    @Override
    public void run() {

        initialMediaCodec();

    }

}
