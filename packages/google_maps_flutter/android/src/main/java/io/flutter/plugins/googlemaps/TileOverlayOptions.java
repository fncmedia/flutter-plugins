package io.flutter.plugins.googlemaps;

import java.util.Map;

public class TileOverlayOptions implements TileOverlayOptionsSink {
    private int color;

    private String baseUrl;

    private Map<String, String> headers;

    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
