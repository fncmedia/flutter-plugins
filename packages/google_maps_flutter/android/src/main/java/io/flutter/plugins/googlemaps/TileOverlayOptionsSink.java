package io.flutter.plugins.googlemaps;

import java.util.Map;

public interface TileOverlayOptionsSink {
    void setColor(int color);

    void setBaseUrl(String baseUrl);

    void setHeaders(Map<String, String> headers);
}
