package org.infobip.voice.genapi;

//import org.infobip.security.secret.SecretInjector;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String... args) {
        new SpringApplicationBuilder(Application.class)
//                .initializers(SecretInjector.newInstance()
//                        .renameKey("DATABASE_URL", "spring.datasource.url")
//                        .renameKey("DATABASE_USER", "spring.datasource.username")
//                        .renameKey("DATABASE_PASSWORD", "spring.datasource.password"))
                .run(args);
    }
}
