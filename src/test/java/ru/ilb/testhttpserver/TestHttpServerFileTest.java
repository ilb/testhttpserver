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

import ru.ilb.testhttpserver.TestHttpServerFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.cache.CacheControlFeature;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author slavb
 */
public class TestHttpServerFileTest {

    private final Client client;

    private final CacheControlFeature cacheControlFeature;

    public TestHttpServerFileTest() {

        cacheControlFeature = new CacheControlFeature();
        cacheControlFeature.setCacheResponseInputStream(true);

        client = ClientBuilder.newBuilder()
                // see https://www.ehcache.org/documentation/3.0/107.html
                //.property("org.apache.cxf.jaxrs.client.cache.CacheControlFeature.config-uri", "ehcache-jsr107-config")
                .register(cacheControlFeature)
                .build();
    }

    @Test
    public void testSomeMethod() throws MalformedURLException, IOException, Exception {

        URI endpointAddress = URI.create("http://localhost:52341/api/endpoint");
        WebTarget target = client.target(endpointAddress);

        Path source = Paths.get(this.getClass().getResource("test.pdf").toURI());
        try ( TestHttpServerFile th = new TestHttpServerFile(endpointAddress.toURL(), source)) {
            // First call
            executeRequest(target, source);
            // Second call should be cached
            executeRequest(target, source);

        }
    }

    private void executeRequest(WebTarget target, Path source) throws IOException {
        Response response = target.request().get();

        Instant expectedLastMod = Files.getLastModifiedTime(source).toInstant().truncatedTo(ChronoUnit.SECONDS);
        assertEquals(expectedLastMod, response.getLastModified().toInstant());

        assertArrayEquals(Files.readAllBytes(source), response.readEntity(byte[].class));

    }
}
