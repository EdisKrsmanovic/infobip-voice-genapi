package org.infobip.voice;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.infobip.spring.remoting.autoconfigure.configserver.ConfigServerInitializer;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        new SpringApplicationBuilder(Application.class)
                .initializers(ConfigServerInitializer.create())
                .run(args);
    }
}
