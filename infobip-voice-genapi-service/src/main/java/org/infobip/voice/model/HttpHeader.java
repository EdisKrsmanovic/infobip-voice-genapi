package org.infobip.voice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpHeader {
    @NotNull(message = "HttpHeader name cannot be null")
    private String name;
    @NotNull(message = "HttpHeader value cannot be null")
    private String value;
}
