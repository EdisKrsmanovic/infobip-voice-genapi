package org.infobip.voice.api;

import org.infobip.voice.Application;
import org.infobip.voice.connector.model.TemplateRequest;
import org.infobip.voice.connector.model.TemplateResponse;
import org.infobip.voice.connector.service.TemplateService;
import org.infobip.common.remoting.core.server.services.internal.RmiInternal;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class EchoTemplateServiceIT {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    /**
     * This should exist because we registered the invoker with the {@link org.infobip.spring.remoting.client.RmiClientInterfaceRegistrar registrar} - see {@link org.infobip.voice.RmiClientConfig}.
     */
    @Autowired
    private TemplateService templateServiceInvoker;

    @Autowired
    private RmiInternal rmiInternalInvoker;

    @Test
    public void echoesRequestText() {

        TemplateResponse response = templateServiceInvoker.getResponse(new TemplateRequest("wohooo!!!"));

        assertThat(response.getResponse()).isEqualTo("wohooo!!!");
    }

    @Test
    public void rmiInternalWorks() {

        assertThat(rmiInternalInvoker.info()).isNotEmpty();
    }
}