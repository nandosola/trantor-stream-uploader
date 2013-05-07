/*
 * Copyright abstra.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.abstra.trantor;

import java.io.IOException;

import javax.servlet.ServletInputStream;

// Copied from https://github.com/DomDerrien/two-tiers-utils
public class MockServletInputStream extends ServletInputStream {
    private StringBuilder stream;
    private int cursor = 0;
    private int limit;

    /** Default constructor. Use <code>reset(String)</code> to set the stream content. */
    public MockServletInputStream() {
        setData("");
    }
    /** Constructor with the initial stream content */
    public MockServletInputStream(String data) {
        setData(data);
    }

    /** Accessor */
    public void setData(String data) {
        stream = new StringBuilder(data);
        limit = stream.length();
    }

    @Override
    public int read() throws IOException {
        if(cursor < limit) {
            char c = stream.charAt(cursor);
            ++ cursor;
            return (int) c;
        }
        return -1;
    }

    /** Return the initial stream content */
    public String getContents() {
        return stream.toString();
    }
    /** Return the yet processed stream content */
    public String getProcessedContents() {
        return stream.substring(0, cursor - 1);
    }
    /** Return the non yet processed stream content */
    public String getNotProcessedContents() {
        return stream.substring(cursor);
    }
}