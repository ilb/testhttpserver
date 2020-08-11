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

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.cache.CacheControlFeature;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import ru.ilb.jfunction.resources.URLToStringFunction;

/**
 *
 * @author slavb
 */
public class TestHttpServerFileTest {

    private final Client client;

    private final CacheControlFeature cacheControlFeature;

//    private final URIConnection uriConn;
    public TestHttpServerFileTest() throws URISyntaxException {

        cacheControlFeature = new CacheControlFeature();
        cacheControlFeature.setCacheResponseInputStream(true);

        // see https://www.ehcache.org/documentation/3.0/107.html
//        URI cacheConfigUri = this.getClass().getClassLoader().getResource("ehcache-jsr107-config.xml").toURI();
        client = ClientBuilder.newBuilder()
                //                .property("org.apache.cxf.jaxrs.client.cache.CacheControlFeature.config-uri", cacheConfigUri.toString())
                .register(cacheControlFeature)
                .build();
    }

    @Test
    public void testServer() throws MalformedURLException, IOException, Exception {

        URI endpointAddress = URI.create("http://localhost:52341/api/endpoint");

        //Path source = Paths.get(this.getClass().getResource("test.pdf").toURI());
        Path source = Files.createTempFile("InputStreamToPathFunctionImpl", ".tmp");
        System.out.println(source.toString());

        // random stream variant http://www.java2s.com/Code/Java/File-Input-Output/Randominputstream.htm
        try (RandomAccessFile writer = new RandomAccessFile(source.toString(), "rw")) {
            writer.seek(1024 * 1024 * 10);
            writer.writeInt(0);
        }

        try (TestHttpServerFile th = new TestHttpServerFile(endpointAddress.toURL(), source)) {
//            //First call
//            executeRequestWithClient(endpointAddress, source);
//            //Second call should be cached
//            executeRequestWithClient(endpointAddress, source);

            executeRequestWithUrlConnection(endpointAddress, source);
            executeRequestWithUrlConnection(endpointAddress, source);

            assertEquals(2, th.getStats(200));
            assertEquals(0, th.getStats(304));
        }
    }

    private void executeRequestWithClient(URI endpointAddress, Path source) throws IOException {
        WebTarget target = client.target(endpointAddress);
        Response response = target.request().get();

        Instant expectedLastMod = Files.getLastModifiedTime(source).toInstant().truncatedTo(ChronoUnit.SECONDS);
        assertEquals(expectedLastMod, response.getLastModified().toInstant());

        assertArrayEquals(Files.readAllBytes(source), response.readEntity(byte[].class
        ));

    }

    private void executeRequestWithUrlConnection(URI endpointAddress, Path source) throws IOException {
        String apply = URLToStringFunction.INSTANCE.apply(endpointAddress.toURL());
//        URL url = new URL(endpointAddress.toURL().toString());
//        URLConnection conn = url.openConnection();
//        Map<String, List<String>> map = conn.getHeaderFields();
//        long kek = conn.getExpiration();
//        map.entrySet().forEach(x -> {
//            System.out.println("key: " + x.getKey() + " ");
//            x.getValue().forEach(y -> {
//                System.out.println("value: " + y + " ");
//            });
//
//        });
    }
}
