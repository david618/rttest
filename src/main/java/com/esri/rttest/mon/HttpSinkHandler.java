package com.esri.rttest.mon;



import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class HttpSinkHandler implements HttpHandler {

    long cnt = 0L;

    public HttpSinkHandler() {
        cnt = 0L;
    }

    public long getCnt() {
        return cnt;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String uriPath = httpExchange.getRequestURI().toString();

        //String contentType = httpExchange.getRequestHeaders().getFirst("Content-type");


        if (uriPath.equalsIgnoreCase("/")) {


            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
                cnt += 1;

            }

            httpExchange.sendResponseHeaders(200, 0);

        } else {
            httpExchange.sendResponseHeaders(406, 0);
        }

        httpExchange.close();

    }
}
