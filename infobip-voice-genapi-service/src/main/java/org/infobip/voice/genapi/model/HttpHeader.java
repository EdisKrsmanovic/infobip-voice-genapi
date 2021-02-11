package org.infobip.voice.genapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpHeader implements Serializable {
    @NotNull(message = "HttpHeader name cannot be null")
    @NotEmpty(message = "HttpHeader name cannot be empty")
    private String name;
    @NotNull(message = "HttpHeader value cannot be null")
    @NotEmpty(message = "HttpHeader value cannot be empty")
    private String value;
}
