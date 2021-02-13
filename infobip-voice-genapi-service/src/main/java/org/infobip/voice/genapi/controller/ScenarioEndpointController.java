package org.infobip.voice.genapi.controller;

import lombok.AllArgsConstructor;
import org.infobip.voice.genapi.service.ScenarioEndpointService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("scenario")
@AllArgsConstructor
public class ScenarioEndpointController {

    private final ScenarioEndpointService scenarioEndpointService;

}
