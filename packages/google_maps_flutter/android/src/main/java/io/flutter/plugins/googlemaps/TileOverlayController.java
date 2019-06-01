package io.flutter.plugins.googlemaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlayOptions;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.googlemaps.tileprovider.TileProvider;

public class TileOverlayController {
    private final MethodChannel methodChannel;
    private GoogleMap googleMap;

    public TileOverlayController(MethodChannel methodChannel) {
        this.methodChannel = methodChannel;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    void setTileOverlay(Object tileOverlayToSet) {
        if (tileOverlayToSet != null) {
            io.flutter.plugins.googlemaps.TileOverlayOptions options
                    = new io.flutter.plugins.googlemaps.TileOverlayOptions();

            Convert.interpretTileOverlayOptions(tileOverlayToSet, options);

            setTileOverlay(options);
        }
    }

    private TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();

    void setTileOverlay(io.flutter.plugins.googlemaps.TileOverlayOptions options) {
        TileProvider mTileProvider = new TileProvider(
                options.getBaseUrl(), options.getHeaders(), options.getColor());

        tileOverlayOptions.tileProvider(mTileProvider);

        googleMap.addTileOverlay(tileOverlayOptions);
    }
}
