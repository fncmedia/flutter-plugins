package io.flutter.plugins.googlemaps.utils;

import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    public static final String METHOD_POST = "POST";

    public static final String METHOD_GET = "GET";

    public static final String METHOD_OPTIONS = "OPTIONS";

    public static final String METHOD_TRACE = "TRACE";

    public static final String METHOD_PUT = "PUT";

    public static final String METHOD_PATCH = "PATCH";

    public static final String METHOD_DELETE = "DELETE";

    public static final String METHOD_HEAD = "HEAD";


    public static Response<String> httpDelete(String url, Map<String, String> headers, Map<String, String> parameters)
            throws IOException {

        InputStream is;

        StringBuilder builder = new StringBuilder();

        Response<String> mResponse = new Response<>();

        Request mRequest = new Request(url, METHOD_DELETE, headers, parameters);

        HttpURLConnection request = getConnection(mRequest);

        mResponse.setStatusCode(request.getResponseCode());

        if (mResponse.isError()) {
            is = request.getErrorStream();
        } else {
            is = request.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            builder.append(inputLine);
        }

        in.close();

        if (mResponse.isError()) {
            mResponse.setErrorMessage(builder.toString());
        } else {
            mResponse.setData(builder.toString());
        }

        return mResponse;
    }

    public static Response<String> httpGet(String url, Map<String, String> headers, Map<String, String> parameters)
            throws IOException {
        return execute(getConnection(new Request(url, METHOD_GET, headers, parameters)));
    }

    public static HttpURLConnection getConnection(Request request)
            throws IOException {

        HttpURLConnection.setFollowRedirects(request.getFollowRedirects());

        StringBuilder urlBuilder = new StringBuilder();

        urlBuilder.append(request.getUrl());

        if(request.hasParameters()) {
            urlBuilder.append("?");
            urlBuilder.append(buildQueryString(request.getParameters()));
        }

        HttpURLConnection httpConnection =
                (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();

        httpConnection.setRequestMethod(request.getMethod());

        if(request.hasHeaders()) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        httpConnection.setUseCaches(request.getCache());

        if(request.isMethod(METHOD_POST) && request.hasBody()) {
            httpConnection.setRequestProperty(
                    "Content-Type", request.getBody().getContentType());

            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            writeBody(httpConnection, request.getBody().getBody());
        }

        return httpConnection;
    }

    private static Response<String> httpPost(Request mRequest) throws IOException {
        return execute(getConnection(mRequest));
    }

    public static Response<String> execute(HttpURLConnection mConnection) throws IOException {

        InputStream is;

        StringBuilder builder = new StringBuilder();

        Response<String> mResponse = new Response<>();

        mResponse.setStatusCode(mConnection.getResponseCode());

        if (mResponse.isError()) {
            is = mConnection.getErrorStream();
        } else {
            is = mConnection.getInputStream();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String inputLine;

        while ((inputLine = in.readLine()) != null)
            builder.append(inputLine);

        in.close();

        if (mResponse.isError()) {
            mResponse.setErrorMessage(builder.toString());
        } else {
            mResponse.setData(builder.toString());
        }

        return mResponse;
    }

    public static Response<String> httpPost(String url, Map<String, String> headers, Map<String, String> parameters, String body)
            throws IOException {

        RequestBody stringRequestBody = new StringRequestBody(body);

        Request mRequest = new Request(url, METHOD_POST, headers, parameters, stringRequestBody);

        return httpPost(mRequest);
    }

    public static Response<String> httpPost(String url, Map<String, String> headers, Map<String, String> parameters, JSONObject body)
            throws IOException {

        RequestBody jsonRequestBody = new JsonRequestBody(body);

        Request mRequest = new Request(url, METHOD_POST, headers, parameters, jsonRequestBody);

        return httpPost(mRequest);
    }

    public static Response<String> httpPost(String url, Map<String, String> headers, Map<String, String> parameters, Map<String, String> body)
            throws IOException {

        RequestBody parameterRequestBody = new ParameterRequestBody(body);

        Request mRequest = new Request(url, METHOD_POST, headers, parameters, parameterRequestBody);

        return httpPost(mRequest);
    }

    private static void writeBody(HttpURLConnection request, String mBody)
            throws IOException {

        byte[] mContent = mBody.getBytes("UTF-8");

        request.setFixedLengthStreamingMode(mContent.length);

        OutputStream os = request.getOutputStream();

        os.write(mContent);
        os.flush();
        os.close();
    }

    private static String buildQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> pair : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static class Response<T> {
        int statusCode = HttpURLConnection.HTTP_OK;

        protected T data;

        String errorMessage;

        boolean mStopPropagation = false;

        Response() {
        }

        public Response(int statusCode, T data) {
            this.statusCode = statusCode;
            this.data = data;
        }


        protected void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        protected void setData(T data) {
            this.data = data;
        }

        void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public T getData() {
            return data;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void stopPropagation() {
            mStopPropagation = true;
        }

        public boolean isStoppedPropagation() {
            return mStopPropagation;
        }

        public boolean isError() {
            return statusCode >= 400;
        }
    }

    public static class Request {
        String url = null;

        Boolean cache = true;

        String method = METHOD_GET;

        Boolean followRedirects = false;

        Map<String, String> parameters = new HashMap<>();

        Map<String, String> headers = new HashMap<>();

        RequestBody body;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Boolean getCache() {
            return cache;
        }

        public void setCache(Boolean cache) {
            this.cache = cache;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        boolean hasParameters() {
            return parameters.size() > 0;
        }

        public void setParameters(Map<String, String> parameters) {
            if(parameters != null) {
                this.parameters = parameters;
            } else {
                this.parameters = new HashMap<>();
            }
        }

        public void addParameter(String name, String value) {
            this.parameters.put(name, value);
        }

        boolean hasHeaders() {
            return headers.size() > 0;
        }

        Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            if(headers != null) {
                this.headers = headers;
            } else {
                this.headers = new HashMap<>();
            }
        }

        public void addHeader(String name, String value) {
            this.headers.put(name, value);
        }

        public boolean hasBody() {
            return body != null;
        }

        RequestBody getBody() {
            return body;
        }

        public void setBody(RequestBody body) {
            this.body = body;
        }

        boolean isMethod(String method) {
            return this.method.equals(method);
        }

        Boolean getFollowRedirects() {
            return followRedirects;
        }

        public void setFollowRedirects(Boolean followRedirects) {
            this.followRedirects = followRedirects;
        }

        Request() {
            headers.put("Accept", "application/json");
            headers.put("Content-value", "application/json;charset=UTF-8");
            headers.put("Accept-Charset", "utf-8");
            headers.put("User-Agent", "Mozilla/5.0 ( compatible ) ");

            headers.put("X-Client-Device-Manufacturer", Build.BRAND);
            headers.put("X-Client-Device-Model", Build.MODEL);
            headers.put("X-Client-Device-Version", Build.VERSION.RELEASE);
        }

        public Request(String url, String method, Map<String, String> headers) {
            this();

            this.url = url;
            this.headers = headers;
            this.method = method;
        }


        Request(String url, String method, Map<String, String> headers, Map<String, String> parameters) {
            this();

            this.url = url;
            this.headers = headers;
            this.method = method;

            setParameters(parameters);
        }

        Request(String url, String method,Map<String, String> headers, Map<String, String> parameters, RequestBody body) {
            this(url, method, headers, parameters);

            this.body = body;
        }
    }

    public interface RequestBody {
        String getContentType();

        String getBody();
    }

    public static class JsonRequestBody implements RequestBody {
        private JSONObject mJson;

        public JsonRequestBody(JSONObject mJson) {
            this.mJson = mJson;
        }

        @Override
        public String getContentType() {
            return "application/json;charset=utf-8";
        }

        @Override
        public String getBody() {
            return mJson.toString();
        }
    }

    public static class StringRequestBody implements RequestBody {
        private String mBody;

        StringRequestBody(String mBody) {
            this.mBody = mBody;
        }

        @Override
        public String getContentType() {
            return "application/json;charset=utf-8";
        }

        @Override
        public String getBody() {
            return mBody;
        }
    }

    public static class ParameterRequestBody implements RequestBody {
        private Map<String, String> mParameters;

        public ParameterRequestBody(Map<String, String> mParameters) {
            this.mParameters = mParameters;
        }

        @Override
        public String getContentType() {
            return "application/x-www-form-urlencoded";
        }

        @Override
        public String getBody() {
            try {
                return buildQueryString(mParameters);
            } catch(Exception ex) {
                return null;
            }
        }
    }
}
