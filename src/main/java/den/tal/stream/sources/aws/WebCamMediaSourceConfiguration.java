package den.tal.stream.sources.aws;

import com.amazonaws.kinesisvideo.internal.client.mediasource.MediaSourceConfiguration;
import com.amazonaws.kinesisvideo.producer.TrackInfo;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@ToString
@Configuration
public class WebCamMediaSourceConfiguration implements MediaSourceConfiguration {

    @Getter
    private TrackInfo[] trackInfoList;

    @Value("${sources.webcam.device.type}")
    @Getter
    private String mediaSourceType;

    @Value("${sources.webcam.device.description}")
    @Getter
    private String mediaSourceDescription;

    @Value("${kinesis.video.stream.content_type}")
    @Getter
    private String contentType;

    @Value("${sources.webcam.device.fps}")
    @Getter
    private int fps;
}
