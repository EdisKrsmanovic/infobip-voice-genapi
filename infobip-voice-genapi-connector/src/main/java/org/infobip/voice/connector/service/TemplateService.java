package org.infobip.voice.connector.service;

import org.infobip.voice.connector.model.TemplateRequest;
import org.infobip.voice.connector.model.TemplateResponse;

public interface TemplateService {

    TemplateResponse getResponse(TemplateRequest request);

}
