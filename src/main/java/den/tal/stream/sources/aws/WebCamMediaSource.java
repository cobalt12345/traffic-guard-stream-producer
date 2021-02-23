package den.tal.stream.sources.aws;

import com.amazonaws.kinesisvideo.client.mediasource.MediaSourceState;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSource;
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceSink;
import com.amazonaws.kinesisvideo.internal.mediasource.DefaultOnStreamDataAvailable;
import com.amazonaws.kinesisvideo.producer.StreamCallbacks;
import com.amazonaws.kinesisvideo.producer.StreamInfo;

import static com.amazonaws.kinesisvideo.producer.StreamInfo.NalAdaptationFlags.*;
import static com.amazonaws.kinesisvideo.producer.StreamInfo.codecIdFromContentType;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.*;

import com.amazonaws.kinesisvideo.producer.Tag;
import den.tal.stream.sources.WebCam;
import den.tal.stream.sources.utils.FrameConverter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class WebCamMediaSource implements MediaSource {

    @Value("${kinesis.video.stream.name}")
    private String kinesisVideoStreamName;

    private WebCam webCam;

    @Autowired
    private FrameConverter frameConverter;
    private MediaSourceState mediaSourceState;
    private WebCamMediaSourceConfiguration webCamMediaSourceConfiguration;
    private final String trackName = "RoadTraffic";
    private MediaSourceSink mediaSourceSink;
    private WebCamImageFrameSource webCamImageFrameSource;
    private static final byte[] AVCC_EXTRA_DATA = {
            (byte) 0x01, (byte) 0x42, (byte) 0x00, (byte) 0x1E, (byte) 0xFF, (byte) 0xE1, (byte) 0x00, (byte) 0x22,
            (byte) 0x27, (byte) 0x42, (byte) 0x00, (byte) 0x1E, (byte) 0x89, (byte) 0x8B, (byte) 0x60, (byte) 0x50,
            (byte) 0x1E, (byte) 0xD8, (byte) 0x08, (byte) 0x80, (byte) 0x00, (byte) 0x13, (byte) 0x88,
            (byte) 0x00, (byte) 0x03, (byte) 0xD0, (byte) 0x90, (byte) 0x70, (byte) 0x30, (byte) 0x00, (byte) 0x5D,
            (byte) 0xC0, (byte) 0x00, (byte) 0x17, (byte) 0x70, (byte) 0x5E, (byte) 0xF7, (byte) 0xC1, (byte) 0xF0,
            (byte) 0x88, (byte) 0x46, (byte) 0xE0, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x28, (byte) 0xCE,
            (byte) 0x1F, (byte) 0x20};

    public WebCamMediaSource(WebCam webCam) {
        this.webCam = webCam;
    }

    @Override
    public MediaSourceState getMediaSourceState() {
        return mediaSourceState;
    }

    @Override
    public MediaSourceConfiguration getConfiguration() {

        return webCamMediaSourceConfiguration;
    }

    @Override
    public StreamInfo getStreamInfo() throws KinesisVideoException {
        return new StreamInfo(VERSION_ZERO,
                kinesisVideoStreamName,
                StreamInfo.StreamingType.STREAMING_TYPE_NEAR_REALTIME,
                webCamMediaSourceConfiguration.getContentType(),
                NO_KMS_KEY_ID,
                RETENTION_ONE_HOUR,
                NOT_ADAPTIVE,
                MAX_LATENCY_ZERO,
                DEFAULT_GOP_DURATION,
                KEYFRAME_FRAGMENTATION,
                USE_FRAME_TIMECODES,
                ABSOLUTE_TIMECODES,
                REQUEST_FRAGMENT_ACKS,
                RECOVER_ON_FAILURE,
                codecIdFromContentType(webCamMediaSourceConfiguration.getContentType()),
                trackName,
                DEFAULT_BITRATE,
                webCamMediaSourceConfiguration.getFps(),
                DEFAULT_BUFFER_DURATION,
                DEFAULT_REPLAY_DURATION,
                DEFAULT_STALENESS_DURATION,
                DEFAULT_TIMESCALE,
                RECALCULATE_METRICS,
                AVCC_EXTRA_DATA,
                new Tag[] {
                        new Tag("device", webCamMediaSourceConfiguration.getMediaSourceDescription()),
                        new Tag("stream", kinesisVideoStreamName)
                },
                NAL_ADAPTATION_ANNEXB_NALS);
    }

    @Override
    public void configure(MediaSourceConfiguration mediaSourceConfiguration) {
        if (mediaSourceConfiguration instanceof WebCamMediaSourceConfiguration) {
            WebCamMediaSourceConfiguration wcmsConf = (WebCamMediaSourceConfiguration) mediaSourceConfiguration;
            webCamMediaSourceConfiguration = wcmsConf;
            log.debug("Web Camera source configuration: {}", mediaSourceConfiguration);
        } else {

            throw new IllegalArgumentException("Configuration must be an instance of WebCamMediaSourceConfiguration");
        }
    }

    @Override
    public void initialize(@NonNull MediaSourceSink mediaSourceSink) throws KinesisVideoException {
        this.mediaSourceSink = mediaSourceSink;
        mediaSourceState = MediaSourceState.INITIALIZED;
    }

    @Override
    public void start() throws KinesisVideoException {
        log.trace("Start webcam media source...");
        webCamImageFrameSource = new WebCamImageFrameSource(webCamMediaSourceConfiguration, webCam, frameConverter);
        webCamImageFrameSource.onStreamDataAvailable(new DefaultOnStreamDataAvailable(mediaSourceSink));
        webCamImageFrameSource.start();
        mediaSourceState = MediaSourceState.RUNNING;
    }

    @Override
    public void stop() throws KinesisVideoException {
        log.trace("Stop webcam media source...");
        if (webCamImageFrameSource != null) {
            webCamImageFrameSource.stop();
        }
        try {
            if (null != mediaSourceSink && null != mediaSourceSink.getProducerStream()) {
                mediaSourceSink.getProducerStream().stopStreamSync();
            }
        } finally {
            mediaSourceState = MediaSourceState.STOPPED;
        }
    }

    @Override
    public boolean isStopped() {
        return mediaSourceState == MediaSourceState.STOPPED;
    }

    @Override
    public void free() throws KinesisVideoException {
        log.trace("Free webcam media source...");
    }

    @Override
    public MediaSourceSink getMediaSourceSink() {
        return mediaSourceSink;
    }

    @Override
    public StreamCallbacks getStreamCallbacks() {
        return null;
    }
}
