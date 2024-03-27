/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * <p>StreamUtils class.</p>
 */
public abstract class StreamUtils {

    /**
     * Convenience method for reading data from a <code>Reader</code> and then
     * immediately writing that data to a <code>Writer</code> with a default
     * buffer size of one kilobyte (1,024 chars).
     *
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @throws java.io.IOException if any.
     */
    public static void streamToStream(Reader in, Writer out) throws IOException {
        streamToStream(in, out, 1024);
    }

    /**
     * Convenience method for reading data from a <code>Reader</code> and then
     * immediately writing that data to a <code>Writer</code>.
     *
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @param bufferSize
     *            the size of the <code>char</code> buffer to use for each
     *            read/write
     * @throws java.io.IOException if any.
     */
    public static void streamToStream(Reader in, Writer out, int bufferSize) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
    
        if (bufferSize < 1) {
            throw new IllegalArgumentException("Cannot take negative buffer size.");
        }
    
        char[] b = new char[bufferSize];
        int length;
    
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

    /**
     * Convenience method for reading data from an <code>InputStream</code>
     * and then immediately writing that data to an <code>OutputStream</code>
     * with a default buffer size of one kilobyte (1,024 bytes).
     *
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @throws java.io.IOException if any.
     */
    public static void streamToStream(InputStream in, OutputStream out) throws IOException {
        streamToStream(in, out, 1024);
    }

    /**
     * Convenience method for reading data from an <code>InputStream</code>
     * and then immediately writing that data to an <code>OutputStream</code>.
     *
     * @param in
     *            a data source
     * @param out
     *            a data sink
     * @param bufferSize
     *            the size of the <code>byte</code> buffer to use for each
     *            read/write
     * @throws java.io.IOException if any.
     */
    public static void streamToStream(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] b = new byte[bufferSize];
        int length;
    
        while ((length = in.read(b)) != -1) {
            out.write(b, 0, length);
        }
    }

}
