package den.tal.stream.sources;

import den.tal.stream.StreamProducerConfig;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringJUnitConfig(StreamProducerConfig.class)
@Log4j2
public class WebCamTest {

    @Autowired
    private WebCam webCam;

    @Test
    public void takeAShot() throws InterruptedException {
        Thread.currentThread().setName("Operator");
        log.info("Start filming...");
        webCam.startFilming();
        log.info("Continues to make movie...");
        TimeUnit.SECONDS.sleep(1);
        log.info("Stop filming...");
        webCam.stopFilming();
        TimeUnit.SECONDS.sleep(5);
        log.info("Start filming again...");
        webCam.startFilming();
        TimeUnit.SECONDS.sleep(1);
        log.info("Switch off the camera.");
        webCam.stopFilming();
        webCam.stopWebCamSource();
    }
}
