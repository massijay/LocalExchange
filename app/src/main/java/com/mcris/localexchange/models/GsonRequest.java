package com.mcris.localexchange.models;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private final Gson gson;
    private final Type type;
    private final T payload;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    public GsonRequest(int method, String url, Map<String, String> headers, Type type,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.gson = new Gson();
        this.type = type;
        this.headers = headers;
        this.listener = listener;
        this.payload = null;
    }

    public GsonRequest(int method, String url, Map<String, String> headers, T payload, Type type,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        this.type = type;
        this.headers = headers;
        this.listener = listener;
        this.payload = payload;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (payload != null) {
            return gson.toJson(payload).getBytes(StandardCharsets.UTF_8);
        }
        return super.getBody();
    }

    @Override
    public String getBodyContentType() {
        if (payload != null) {
            return "application/json; charset=" + getParamsEncoding();
        }
        return super.getBodyContentType();
    }

    @Override
    protected String getParamsEncoding() {
        if (payload != null) {
            return StandardCharsets.UTF_8.toString();
        }
        return super.getParamsEncoding();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, type),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}