package htc_ericli.mediacodecplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioDecoderThread  extends Thread{

    private static final String TAG = MainActivity.class.getName();
    public long timeoutUs = 0;

    private int mSampleRate = 0;

    public AudioTrack playAudioTrack;
    public MediaExtractor extractor;
    public MediaCodec codec;

    public void MediaCodecConfigure(Surface surface,MediaExtractor myExtractor,String[] mine,MediaFormat[] format,int i){

        extractor = myExtractor;
        extractor.selectTrack(i);

        try {
            codec = MediaCodec.createDecoderByType(mine[i]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSampleRate = format[i].getInteger(MediaFormat.KEY_SAMPLE_RATE);

        codec.configure(format[i],null,null,0);

        // create our AudioTrack instance
        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = 4 * minBufferSize;
        playAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );
        playAudioTrack.play();

        codec.start();


    }

    public void initialMediaCodec(){

        boolean isEOS = false;

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        for (;;) {

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

                    byte[] chunk = new byte[info.size];
                    outputBuffer.get(chunk);
                    outputBuffer.clear();
                    if(chunk.length>0){
                        playAudioTrack.write(chunk,0,chunk.length);
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, 0);
                }

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // can ignore if API level >= 21 and using getOutputFormat(outputBufferIndex)
                playAudioTrack.setPlaybackRate(mSampleRate);
                MediaFormat format = codec.getOutputFormat();
                Log.d(TAG, "output format has changed to " + format);
            }else {
                Log.d(TAG, "dequeueOutputBuffer returned " + outputBufferIndex);
            }

        }

        codec.stop();
        codec.release();
        codec = null;

        playAudioTrack.stop();

    }

    @Override
    public void run() {

        initialMediaCodec();

    }

}
