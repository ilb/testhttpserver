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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author slavb
 */
public class TestHttpServerFile extends TestHttpServer {

    private static final Logger LOG = Logger.getLogger(TestHttpServerFile.class.getName());
    private final Path contentsPath;
    private final Map<Integer, Integer> stats = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    private final static String LAST_MODIFIED = "Last-Modified";
    private final static String CONTENT_TYPE = "Content-Type";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_CONTROL_DEFAULT_VALUE = "max-age=1, must-revalidate";
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public TestHttpServerFile(URL url, Path contentsPath) throws IOException {
        super(url);
        this.contentsPath = contentsPath;
        headers.put(CONTENT_TYPE, "application/octet-stream"); // Files.probeContentType(contentsPath)
        headers.put(CACHE_CONTROL, CACHE_CONTROL_DEFAULT_VALUE);
    }

    public TestHttpServerFile(URL url, Path contentsPath, Map<String, String> headers) throws IOException {
        super(url);
        this.contentsPath = contentsPath;
        headers.putAll(headers);
    }

    @Override
    void handle(HttpExchange exchange) throws IOException {
        Instant lastModifiedServer = Files.getLastModifiedTime(contentsPath).toInstant().truncatedTo(ChronoUnit.SECONDS);

        Headers requestHeaders = exchange.getRequestHeaders();
        Instant lastModifiedClient = getIfModifiedSince(requestHeaders);
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add(LAST_MODIFIED, formatLastModifiedHeader(lastModifiedServer));

        // file is not modified since last request
        if (lastModifiedClient != null && lastModifiedClient.compareTo(lastModifiedServer) >= 0) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_MODIFIED, -1);
            LOG.log(Level.INFO, "{0} HTTP_NOT_MODIFIED", new Object[]{url});
            updateStats(HttpURLConnection.HTTP_NOT_MODIFIED);
        } else {
            this.headers.entrySet().forEach(e -> responseHeaders.add(e.getKey(),e.getValue()));

            byte[] response = Files.readAllBytes(contentsPath);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            LOG.log(Level.INFO, "{0} HTTP_OK", new Object[]{url});
            updateStats(HttpURLConnection.HTTP_OK);
        }
        exchange.close();
    }

    private int updateStats(int httpCode) {
        return stats.merge(httpCode, 1, Integer::sum);
    }

    public int getStats(int httpCode) {
        return stats.getOrDefault(httpCode, 0);
    }

    private static String formatLastModifiedHeader(Instant instant) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(instant.atZone(ZoneId.of("UTC")));
    }

    private static Instant getIfModifiedSince(Headers headers) {
        return Optional.ofNullable(headers.getFirst(IF_MODIFIED_SINCE)).map(s -> parseRFC1132Date(s)).orElse(null);
    }

    private static Instant parseRFC1132Date(String date) {
        return Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date));
    }
}
