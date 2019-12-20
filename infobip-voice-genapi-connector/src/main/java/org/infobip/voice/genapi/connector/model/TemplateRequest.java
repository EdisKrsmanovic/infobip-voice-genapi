package org.infobip.voice.genapi.connector.model;

public class TemplateRequest {

    private String request;

    public TemplateRequest() {
    }

    public TemplateRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateRequest that = (TemplateRequest) o;

        return !(request != null ? !request.equals(that.request) : that.request != null);

    }

    @Override
    public int hashCode() {
        return request != null ? request.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TemplateRequest{" +
                "request='" + request + '\'' +
                '}';
    }
}
