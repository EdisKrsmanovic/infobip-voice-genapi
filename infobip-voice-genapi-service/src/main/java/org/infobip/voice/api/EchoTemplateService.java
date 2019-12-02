package org.infobip.voice.api;

import org.infobip.voice.connector.model.TemplateRequest;
import org.infobip.voice.connector.model.TemplateResponse;
import org.infobip.voice.connector.service.TemplateService;
import org.infobip.spring.remoting.server.export.Export;
import org.springframework.stereotype.Service;

@Service
@Export(TemplateService.class)
public class EchoTemplateService implements TemplateService {

    @Override
    public TemplateResponse getResponse(TemplateRequest request) {
        return new TemplateResponse(request.getRequest());
    }

}
