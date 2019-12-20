package org.infobip.voice.genapi;

import org.infobip.voice.genapi.connector.service.TemplateService;
import org.infobip.common.remoting.core.server.services.internal.RmiInternal;
import org.infobip.spring.remoting.client.RmiClientInterfaceRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration {

    @Bean
    public RmiClientInterfaceRegistrar registrar() {

        RmiClientInterfaceRegistrar registrar = new RmiClientInterfaceRegistrar();

        registrar.addModule("self")
                .addInterface("templateServiceInvoker", TemplateService.class)
                .addInterface("rmiInternalInvoker", RmiInternal.class);

        return registrar;
    }

}
