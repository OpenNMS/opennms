/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class XdrDataOutputStream extends DataOutputStream {

    private final XdrOutputStream mOut;

    public XdrDataOutputStream(OutputStream out) {
        super(new XdrOutputStream(out));
        mOut = (XdrOutputStream) super.out;
    }

    public void writeString(String s) throws IOException {
        byte[] chars = s.getBytes(StandardCharsets.UTF_8);
        writeInt(chars.length);
        write(chars);
    }

    public void writeIntArray(int[] a) throws IOException {
        writeInt(a.length);
        writeIntArray(a, a.length);
    }

    private void writeIntArray(int[] a, int l) throws IOException {
        for (int i = 0; i < l; i++) {
            writeInt(a[i]);
        }
    }

    public void writeFloatArray(float[] a) throws IOException {
        writeInt(a.length);
        writeFloatArray(a, a.length);
    }

    private void writeFloatArray(float[] a, int l) throws IOException {
        for (int i = 0; i < l; i++) {
            writeFloat(a[i]);
        }
    }

    public void writeDoubleArray(double[] a) throws IOException {
        writeInt(a.length);
        writeDoubleArray(a, a.length);
    }

    private void writeDoubleArray(double[] a, int l) throws IOException {
        for (int i = 0; i < l; i++) {
            writeDouble(a[i]);
        }
    }

    private static final class XdrOutputStream extends OutputStream {

        private final OutputStream mOut;

        // Number of bytes written
        private int mCount;

        public XdrOutputStream(OutputStream out) {
            mOut = out;
            mCount = 0;
        }

        @Override
        public void write(int b) throws IOException {
            mOut.write(b);
            // https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html#write(int):
            // > The byte to be written is the eight low-order bits of the argument b.
            // > The 24 high-order bits of b are ignored.
            mCount++;
        }

        @Override
        public void write(byte[] b) throws IOException {
            // https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html#write(byte[]):
            // > The general contract for write(b) is that it should have exactly the same effect
            // > as the call write(b, 0, b.length).
            write(b, 0, b.length);
        }

        public void write(byte[] b, int offset, int length) throws IOException {
            mOut.write(b, offset, length);
            mCount += length;
            pad();
        }

        public void pad() throws IOException {
            int pad = 0;
            int mod = mCount % 4;
            if (mod > 0) {
                pad = 4 - mod;
            }
            while (pad-- > 0) {
                write(0);
            }
        }
    }
}
