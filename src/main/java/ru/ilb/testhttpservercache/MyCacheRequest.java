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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.util.List;
import java.util.Map;

class MyCacheRequest extends CacheRequest {

    FileOutputStream fos;
    private File file;
    private boolean isError = false;

    public MyCacheRequest(String filename, Map<String, List<String>> rspHeaders) {
        try {
            file = new File(filename);
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(rspHeaders);
        } catch (Exception ex) {
            ex.printStackTrace();
            isError = true;
        }
    }

    public boolean isError() {
        return isError;
    }

    @Override
    public OutputStream getBody() throws IOException {
        return fos;
    }

    @Override
    public void abort() {
        try {
            fos.close();
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
