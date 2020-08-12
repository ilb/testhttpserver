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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.CacheResponse;
import java.util.List;
import java.util.Map;

class MyCacheResponse extends CacheResponse {

    FileInputStream fis;
    Map<String, List<String>> headers;
    boolean isError = false;

    public MyCacheResponse(String filename) {
        try {
            fis = new FileInputStream(new File(filename));
            ObjectInputStream ois = new ObjectInputStream(fis);
            headers = (Map<String, List<String>>) ois.readObject();
        } catch (Exception ex) {
            isError = true;
        }
    }

    public boolean getIsError() {
        return isError;
    }

    @Override
    public InputStream getBody() throws IOException {
        return fis;
    }

    @Override
    public Map getHeaders() throws IOException {
        return headers;
    }
}
