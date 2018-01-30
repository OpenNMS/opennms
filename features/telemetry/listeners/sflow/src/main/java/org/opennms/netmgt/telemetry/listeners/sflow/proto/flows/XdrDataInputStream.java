package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class XdrDataInputStream extends DataInputStream {

    // The underlying input stream
    private final XdrInputStream mIn;

    /**
     * Creates a XdrDataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public XdrDataInputStream(InputStream in) {
        super(new XdrInputStream(in));
        mIn = (XdrInputStream) super.in;
    }

    public String readString() throws IOException {
        int l = readInt();
        byte[] bytes = new byte[l];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int[] readIntArray() throws IOException {
        int l = readInt();
        return readIntArray(l);
    }

    private int[] readIntArray(int l) throws IOException {
        int[] arr = new int[l];
        for (int i = 0; i < l; i++) {
            arr[i] = readInt();
        }
        return arr;
    }

    public float[] readFloatArray() throws IOException {
        int l = readInt();
        return readFloatArray(l);
    }

    private float[] readFloatArray(int l) throws IOException {
        float[] arr = new float[l];
        for (int i = 0; i < l; i++) {
            arr[i] = readFloat();
        }
        return arr;
    }

    public double[] readDoubleArray() throws IOException {
        int l = readInt();
        return readDoubleArray(l);
    }

    private double[] readDoubleArray(int l) throws IOException {
        double[] arr = new double[l];
        for (int i = 0; i < l; i++) {
            arr[i] = readDouble();
        }
        return arr;
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    /**
     * Need to provide a custom impl of InputStream as DataInputStream's read methods
     * are final and we need to keep track of the count for padding purposes.
     */
    private static final class XdrInputStream extends InputStream {

        // The underlying input stream
        private final InputStream mIn;

        // The amount of bytes read so far.
        private int mCount;

        public XdrInputStream(InputStream in) {
            mIn = in;
            mCount = 0;
        }

        @Override
        public int read() throws IOException {
            int read = mIn.read();
            if (read >= 0) {
                mCount++;
            }
            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = mIn.read(b, off, len);
            mCount += read;
            pad();
            return read;
        }

        public void pad() throws IOException {
            int pad = 0;
            int mod = mCount % 4;
            if (mod > 0) {
                pad = 4-mod;
            }

            while (pad-- > 0) {
                int b = read();
                if (b != 0) {
                    throw new IOException("non-zero padding");
                }
            }
        }
    }
}
