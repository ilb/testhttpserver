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
public abstract class TestHttpServer implements AutoCloseable {

    protected final URL url;
    private final HttpServer httpServer;

    public TestHttpServer(URL url) throws IOException {
        this.url = url;
        httpServer = createServer(url);
        httpServer.createContext(url.getPath(), this::handle);
        httpServer.start();
    }


    abstract void handle (HttpExchange exchange) throws IOException;

    private HttpServer createServer(URL url) throws IOException {
        return HttpServer.create(new InetSocketAddress(url.getPort() != -1 ? url.getPort() : url.getDefaultPort()), 0);
    }

    @Override
    public void close() throws Exception {
        httpServer.stop(0);
    }

}
