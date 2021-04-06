package com.esri.rttest.mon;



import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class HttpSinkHandler implements HttpHandler {

    long cnt = 0L;
    boolean printMessages;

    public HttpSinkHandler(boolean printMessages) {
        cnt = 0L;
        this.printMessages = printMessages;
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

                if (printMessages) {
                    byte[] requestBytes = new byte[100];
                    int numBytes = httpExchange.getRequestBody().read(requestBytes, 0, 100);
                    String more = "";
                    if (numBytes == -1) {
                        more = "...";
                    }
                    System.out.println(requestBytes.toString() + more);
                }

            }

            httpExchange.sendResponseHeaders(200, 0);

        } else {
            httpExchange.sendResponseHeaders(406, 0);
        }

        httpExchange.close();

    }
}
