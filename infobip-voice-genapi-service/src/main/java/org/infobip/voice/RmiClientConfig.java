package org.infobip.voice;

import org.infobip.common.remoting.core.server.services.internal.RmiInternal;
import org.infobip.spring.remoting.client.RmiClientInterfaceRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmiClientConfig {

    @Bean
    public RmiClientInterfaceRegistrar registrar() {

        RmiClientInterfaceRegistrar registrar = new RmiClientInterfaceRegistrar();
        // IMPORTANT: remove 'infobipremotingservice' module in production; this is only example how to configure RMI client
//        registrar.addModule("infobipremotingservice") // module name should be the same as configuration prefix (see application-development.yml#26)
//                .addInterface(RmiInternal.class); // addInterface method can be chained

        return registrar;
    }
}
