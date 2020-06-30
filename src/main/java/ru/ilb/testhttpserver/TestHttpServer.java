/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.testhttpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 *
 * @author slavb
 */
public class TestHttpServer implements AutoCloseable {

    private final HttpServer httpServer;

    public TestHttpServer(URL url, HttpHandler handler) throws IOException {
        httpServer = createServer(url);
        httpServer.createContext(url.getPath(), handler);
        httpServer.start();
    }

    public TestHttpServer(URL url, String content) throws IOException {
        this(url, (HttpExchange exchange) -> {
            byte[] response = content.getBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
    }

    private HttpServer createServer(URL url) throws IOException {
        return HttpServer.create(new InetSocketAddress(url.getPort() != -1 ? url.getPort() : url.getDefaultPort()), 0);
    }

    @Override
    public void close() throws Exception {
        httpServer.stop(0);
    }

}
