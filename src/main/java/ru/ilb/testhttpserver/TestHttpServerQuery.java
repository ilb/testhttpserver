/*
 * Copyright 2020 slavb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.testhttpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class TestHttpServerQuery extends TestHttpServer {

    private final static String CONTENT_TYPE = "Content-Type";

    public TestHttpServerQuery(URL url) throws IOException {
        super(url);
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        Map<String, String> map = queryToMap(exchange.getRequestURI().getQuery());
        JSONObject json = new JSONObject(map);
        byte[] response = json.toString().getBytes();
        Headers responseHeaders = exchange.getResponseHeaders();

        responseHeaders.add(CONTENT_TYPE, "application/json");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);

        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private final static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
