package io.flutter.plugins.googlemaps.tileprovider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geometry.Point;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.plugins.googlemaps.utils.HttpUtils;


public class TileProvider extends AbstractTileProvider<PolylineOptions> {
    private int color;

    public TileProvider(String url, Map<String, String> headers, int color) {
        super(url, headers);

        this.color = color;
    }

    protected void drawCanvasFromArray(Canvas c, List<PolylineOptions> polylines, float scale) {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setShadowLayer(0, 0, 0, 0);
        paint.setAntiAlias(true);

        if (polylines != null) {
            for (int i = 0; i < polylines.size(); i++) {
                List<LatLng> route = polylines.get(i).getPoints();
                paint.setColor(polylines.get(i).getColor());
                paint.setStrokeWidth(getLineWidth(polylines.get(i).getWidth(), scale));
                Path path = new Path();
                if (route != null && route.size() > 1) {
                    Point screenPt1 = mProjection.toPoint(route.get(0)); //first point
                    MarkerOptions m = new MarkerOptions();
                    m.position(route.get(0));
                    path.moveTo((float) screenPt1.x, (float) screenPt1.y);
                    for (int j = 1; j < route.size(); j++) {
                        Point screenPt2 = mProjection.toPoint(route.get(j));
                        path.lineTo((float) screenPt2.x, (float) screenPt2.y);
                    }
                }
                c.drawPath(path, paint);
            }
        }
    }

    public List<PolylineOptions> request(int x, int y, int z) {

        if(z  > 10) {
            x = (int) (x / Math.pow(2, z - 10));
            y = (int) (y / Math.pow(2, z - 10));
            z = 10;
        }

        String key = String.format("%s:%s:%s", x, y, z);

        if(requestCache.get(key) != null) {
            return requestCache.get(key);
        }

        Log.v("FNCDUMP", String.format(url, x, y, z));

        HttpUtils.Request mRequest = new HttpUtils.Request(
                String.format(url, x, y, z), HttpUtils.METHOD_GET, headers);

        List<PolylineOptions> polylines = new ArrayList<>();

        try {
            HttpUtils.Response<String> response
                    = HttpUtils.execute(HttpUtils.getConnection(mRequest));

            JSONObject object = new JSONObject(response.getData());

            JSONArray features = object.getJSONArray("features");

            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = (JSONObject) features.get(i);

                JSONArray coordinates = feature.getJSONObject("geometry")
                        .getJSONArray("coordinates");

                PolylineOptions options = new PolylineOptions();

                for (int f = 0; f < coordinates.length(); f++) {
                    JSONArray item = (JSONArray) coordinates.get(f);

                    options.add(new LatLng(item.getDouble(1), item.getDouble(0)));
                }

                options.color(color);

                polylines.add(options);
            }


            requestCache.put(key, polylines);

        } catch (Exception e) {

        }

        return polylines;
    }
}