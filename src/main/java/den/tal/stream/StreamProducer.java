package den.tal.stream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.java.client.KinesisVideoJavaClientFactory;
import com.amazonaws.regions.Regions;
import den.tal.stream.sources.aws.WebCamMediaSource;
import den.tal.stream.sources.aws.WebCamMediaSourceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StreamProducer {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.user.profile}")
    private String userProfile;

    @Autowired
    private WebCamMediaSource webCamMediaSource;

    @Autowired
    private WebCamMediaSourceConfiguration webCamMediaSourceConfiguration;

    public void startStreaming() throws KinesisVideoException {
        var kinesisVideoClient =
                KinesisVideoJavaClientFactory.createKinesisVideoClient(Regions.fromName(awsRegion),
                new ProfileCredentialsProvider(userProfile));

        webCamMediaSource.configure(webCamMediaSourceConfiguration);
        kinesisVideoClient.registerMediaSource(webCamMediaSource);
        webCamMediaSource.start();
    }
}
