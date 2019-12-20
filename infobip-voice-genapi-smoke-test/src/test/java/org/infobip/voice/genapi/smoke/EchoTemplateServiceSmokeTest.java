package org.infobip.voice.genapi.smoke;

import org.infobip.voice.genapi.connector.model.TemplateRequest;
import org.infobip.voice.genapi.connector.model.TemplateResponse;
import org.infobip.voice.genapi.connector.service.TemplateService;
import org.infobip.common.smoketest.SmokeTestBeanProvider;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class EchoTemplateServiceSmokeTest {
    @Test
    public void name() {
        final TemplateService service = SmokeTestBeanProvider.INSTANCE.getBean(TemplateService.class);
        final TemplateResponse response = service.getResponse(new TemplateRequest());
        assertNotNull(response);
    }
}
