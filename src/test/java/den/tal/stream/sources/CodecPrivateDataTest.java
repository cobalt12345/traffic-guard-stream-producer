package den.tal.stream.sources;

import com.amazonaws.kinesisvideo.parser.utilities.H264FrameEncoder;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;

import java.awt.*;

@Log4j2
public class CodecPrivateDataTest {
    private static final int WIDTH = 1024;
    private static final int HEIGTH = 768;
    private static final int FRAME_RATE = 30;
    private static final Dimension WEBCAM_DIMENSION = new Dimension(WIDTH, HEIGTH);

    @Test
    public void extractCodecPrivateData() {
        var encoder = new H264FrameEncoder(WIDTH, HEIGTH, FRAME_RATE);
        byte[] codecPrivateData = encoder.getCodecPrivateData();

        var sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < codecPrivateData.length; i++) {
            byte codecPrivateDatum = codecPrivateData[i];
            String hexCodecPrivateDatum = Integer.toHexString(codecPrivateDatum).toUpperCase();
            hexCodecPrivateDatum = " (byte) 0x" + (hexCodecPrivateDatum.length() >= 2 ? hexCodecPrivateDatum.substring(
                    hexCodecPrivateDatum.length() - 2) : "0".concat(hexCodecPrivateDatum));
            sb.append(hexCodecPrivateDatum).append(",").append("\n");
        }
        sb.append("}");

        log.info("Codec private data for {}x{}@{}fps:\n {}", WIDTH, HEIGTH, FRAME_RATE, sb);
    }

}
