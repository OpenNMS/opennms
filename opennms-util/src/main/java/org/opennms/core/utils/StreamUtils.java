/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
