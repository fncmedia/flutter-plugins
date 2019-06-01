package io.flutter.plugins.googlemaps.tileprovider;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.google.android.gms.maps.model.Tile;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import io.flutter.plugins.googlemaps.utils.LRUCache;

abstract class AbstractTileProvider<T> implements AuthenticatedTileProviderInterface {

    protected LRUCache<String, Tile> cache = new LRUCache<>(5000);

    protected LRUCache<String, List<T>> requestCache = new LRUCache<>(5000);

    protected final int mTileSize = 512;
    protected final SphericalMercatorProjection mProjection = new SphericalMercatorProjection(mTileSize);
    protected final int mScale = 2;
    protected final int mDimension = mScale * mTileSize;

    protected String url;
    protected Map<String, String> headers;

    public AbstractTileProvider(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    protected float getLineWidth(float width, float scale) {
        return width / (scale);
    }

    @Override
    public synchronized Tile getTile(int x, int y, int zoom) {
        String key = String.format("%s:%s:%s", x, y, zoom);

        if(cache.get(key) != null) {
            return cache.get(key);
        }

        List<T> polylines;

        polylines = request(x, y, zoom);

        if(polylines.size() == 0) {
            return NO_TILE;
        }

        Matrix matrix = new Matrix();
        float scale = ((float) Math.pow(2, zoom) * mScale);
        matrix.postScale(scale, scale);
        matrix.postTranslate(-x * mDimension, -y * mDimension);
        Bitmap bitmap = Bitmap.createBitmap(mDimension, mDimension, Bitmap.Config.ARGB_8888); //save memory on old phones
        Canvas c = new Canvas(bitmap);
        c.setMatrix(matrix);
        drawCanvasFromArray(c, polylines, scale);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        Tile tmp = new Tile(mDimension, mDimension, baos.toByteArray());

        cache.put(key, tmp);

        return tmp;
    }

    protected abstract List<T> request(int x, int y, int z);

    protected abstract void drawCanvasFromArray(Canvas c, List<T> polylines, float scale);
}
