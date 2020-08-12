/*
 * Copyright 2020 andrewsych.
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
package ru.ilb.testhttpservercache;

import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MyResponseCache extends ResponseCache {

    private static final String prefix = "res" + File.separator + "cache" + File.separator + "infoboxcache";
    private static final String ext = ".cache";

    static {
        new File(prefix).mkdirs();
    }

    @Override
    public CacheResponse get(URI uri, String rqstMethod, Map rqstHeaders) throws IOException {
        String filename = getHash(uri.toString());
        MyCacheResponse myCacheResponse = new MyCacheResponse(prefix + File.separator + filename + ext);
        if (myCacheResponse.getIsError()) {
            return null;
        }
        return myCacheResponse;
    }

    @Override
    public CacheRequest put(URI uri, URLConnection conn) throws IOException {
        String filename = getHash(uri.toString());
        MyCacheRequest myCacheRequest = new MyCacheRequest(prefix + File.separator + filename + ext,
                conn.getHeaderFields());
        if (myCacheRequest.isError()) {
            return null;
        }
        return myCacheRequest;
    }

    private String getHash(String text) {
        try {
            byte[] b = createHash(text, "SHA-1");
            return asHex(b);
        } catch (Exception e) {
            return null;
        }
    }

    private String asHex(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    private byte[] createHash(String text, String method) {
        try {
            byte[] b = text.getBytes();
            MessageDigest algorithm = MessageDigest.getInstance(method);
            algorithm.reset();
            algorithm.update(b);
            byte messageDigest[] = algorithm.digest();
            return messageDigest;
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
    }
}
