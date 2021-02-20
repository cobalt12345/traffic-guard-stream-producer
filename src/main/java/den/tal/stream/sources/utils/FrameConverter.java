package den.tal.stream.sources.utils;

import com.amazonaws.kinesisvideo.producer.KinesisVideoFrame;
import den.tal.stream.sources.aws.WebCamMediaSourceConfiguration;
import lombok.extern.log4j.Log4j2;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;
import static com.amazonaws.kinesisvideo.producer.FrameFlags.*;
import static com.amazonaws.kinesisvideo.producer.Time.*;

@Log4j2
@Component
public class FrameConverter {

    private H264Encoder encoder = H264Encoder.createH264Encoder();
    private ColorSpace colorSpace;
    {
        ColorSpace[] supportedColorSpaces = encoder.getSupportedColorSpaces();
        colorSpace = supportedColorSpaces[0];
        log.debug("Supported ColorSpaces: {}", Arrays.toString(supportedColorSpaces));
        log.debug("Use color space: {}", colorSpace);
    }

    private static final long FRAME_DURATION_20_MS = 20L;

    private WebCamMediaSourceConfiguration configuration;

    public FrameConverter(WebCamMediaSourceConfiguration configuration) {
        this.configuration = configuration;
    }

    public KinesisVideoFrame imageToKinesisFrame(BufferedImage image, int counter) {
        Picture picture = AWTUtil.fromBufferedImage(image, colorSpace);
        int buffSize = encoder.estimateBufferSize(picture);
        ByteBuffer byteBuffer = ByteBuffer.allocate(buffSize);
        var encodedFrame = encoder.encodeFrame(picture, byteBuffer);

        final long currentTimeMs = System.currentTimeMillis();
        final int flag = counter % configuration.getFps() == 0 ? FRAME_FLAG_KEY_FRAME : FRAME_FLAG_NONE;
        var kinesisVideoFrame = new KinesisVideoFrame(counter,
                flag,
                currentTimeMs * HUNDREDS_OF_NANOS_IN_A_MILLISECOND,
                currentTimeMs * HUNDREDS_OF_NANOS_IN_A_MILLISECOND,
                FRAME_DURATION_20_MS * HUNDREDS_OF_NANOS_IN_A_MILLISECOND,
                encodedFrame.getData());

        return kinesisVideoFrame;
    }
}
