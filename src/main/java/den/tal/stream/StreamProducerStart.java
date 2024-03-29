package den.tal.stream;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Iterator;

@Log4j2
public class StreamProducerStart {
    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(StreamProducerConfig.class);

        if (log.isDebugEnabled()) {
            for (Iterator<String> namesIter = applicationContext.getBeanFactory().getBeanNamesIterator();
                 namesIter.hasNext(); ) {

                log.debug("Bean: {}", namesIter.next());
            }
        }
        applicationContext.getBean(StreamProducer.class).startStreaming();
    }
}
