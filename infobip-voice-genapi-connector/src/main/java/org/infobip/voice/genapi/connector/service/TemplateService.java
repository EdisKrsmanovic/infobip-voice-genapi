package org.infobip.voice.genapi.connector.service;

import org.infobip.voice.genapi.connector.model.TemplateRequest;
import org.infobip.voice.genapi.connector.model.TemplateResponse;

public interface TemplateService {

    TemplateResponse getResponse(TemplateRequest request);

}
