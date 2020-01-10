package org.infobip.voice.genapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.infobip.spring.remoting.autoconfigure.configserver.ConfigServerInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String... args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(ConfigServerInitializer.create())
                .run(args);
    }
}
