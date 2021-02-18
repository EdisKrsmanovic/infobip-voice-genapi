package edis.krsmanovic.genapi.model;

import java.util.List;

public interface Endpoint {
    Integer getId();
    HttpMethod getHttpMethod();
    List<HttpHeader> getHttpHeaders();
    void setId(Integer id);
}
