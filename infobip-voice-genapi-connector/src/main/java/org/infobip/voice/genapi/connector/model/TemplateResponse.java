package org.infobip.voice.genapi.connector.model;

public class TemplateResponse {

    private String response;

    public TemplateResponse() {
    }

    public TemplateResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateResponse that = (TemplateResponse) o;

        return !(response != null ? !response.equals(that.response) : that.response != null);

    }

    @Override
    public int hashCode() {
        return response != null ? response.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TemplateResponse{" +
                "response='" + response + '\'' +
                '}';
    }
}
