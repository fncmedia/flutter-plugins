part of google_maps_flutter;

class TileOverlay {
  TileOverlay(this.baseUrl, this.color, this.headers);

  final String baseUrl;

  final Color color;

  final Map<String, String> headers;

  dynamic _toJson() {
    final Map<String, dynamic> json = <String, dynamic>{};

    void addIfPresent(String fieldName, dynamic value) {
      if (value != null) {
        json[fieldName] = value;
      }
    }

    addIfPresent('baseUrl', baseUrl);
    addIfPresent('color', color.value);
    addIfPresent('headers', headers);

    return json;
  }
}

Map<String, dynamic> _serializeTileOverlay(TileOverlay tileOverlay) {
  if (tileOverlay == null) {
    return null;
  }

  return tileOverlay._toJson();
}