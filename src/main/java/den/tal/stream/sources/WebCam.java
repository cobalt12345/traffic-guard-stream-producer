package den.tal.stream.sources;

import com.github.sarxos.webcam.Webcam;
import den.tal.stream.sources.exceptions.WebCameraInitializationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Web camera component.<br/>
 * Takes pictures and places them into the queue when started.
 *
 * @author dtalochk
 * @since 0.0.1
 */
@Log4j2
@Component
public class WebCam implements Source {

    @Value("${sources.webcam.queue_length}")
    private int queueLength;

    @Value("${sources.webcam.device.name}")
    private String webCamName;

    @Value("${sources.webcam.device.fps}")
    private int fps;

    private BlockingQueue<BufferedImage> bufferedImages;
    private ExecutorService streamer;
    private Webcam webcam;
    private ReentrantLock filmingStarted = new ReentrantLock();

    @PostConstruct
    void initWebCamSource() throws WebCameraInitializationException {
        log.debug("Init parameters: queue_length={}, webCamName={}, fps={}", queueLength, webCamName, fps);
        log.debug("Init webcam '{}'...\n", webCamName);
        log.debug("Found web cameras: {}", Webcam.getWebcams());
        if (null == (webcam = Webcam.getWebcamByName(webCamName))) {
            log.debug("No camera '{}' found. Try to use default camera...", webCamName);
            webcam = Webcam.getDefault();
        }
        if (null == webcam) {
            log.error("Neither '{}', nor default web cameras were found!", webCamName);

            throw new WebCameraInitializationException("No camera found!");
        }
        webcam.open();
        bufferedImages = new ArrayBlockingQueue<BufferedImage>(queueLength);
    }

    @PreDestroy
    void stopWebCamSource() {
        log.debug("Stop webcam {}", webCamName);
        streamer.shutdownNow();
        webcam.close();
    }

    /**
     * Does the real filming.
     */
    protected class Shooter implements Runnable {
        @Override
        public void run() {
            var threadName = Thread.currentThread().getName();
            while (true) {
                filmingStarted.lock();
                try {
                    log.debug("Shoot", threadName);
                    if (bufferedImages.remainingCapacity() > 0) {
                        bufferedImages.offer(webcam.getImage());
                    } else {
                        log.warn("Not enough film!");
                    }
                    TimeUnit.MILLISECONDS.sleep(1000/fps);
                } catch (InterruptedException iex) {
                    log.warn("{} interrupted");
                } finally {
                    filmingStarted.unlock();
                }
            }
        }
    }

    /**
     * Start filming.
     */
    public void startFilming() {
        String callerName = Thread.currentThread().getName();
        if (null == streamer) {
            log.debug("First filming start by {}", callerName);
            streamer = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable,
                    "Web camera [".concat(webCamName).concat("]")));

            streamer.submit(new Shooter());
        } else {
            filmingStarted.unlock();
            log.debug("Filming restarted by {}", callerName);
        }
    }

    /**
     * Temporary stop filming.
     */
    public void stopFilming() {
        filmingStarted.lock();
        log.debug("Filming stopped by {}", Thread.currentThread().getName());
    }

    /**
     * Get camera film.
     *
     * @return queue containing buffered images.
     */
    public BlockingQueue<BufferedImage> getFilm() {

        return bufferedImages;
    }
}
