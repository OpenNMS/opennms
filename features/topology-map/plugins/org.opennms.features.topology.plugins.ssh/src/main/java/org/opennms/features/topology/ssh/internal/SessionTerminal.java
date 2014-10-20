package org.opennms.features.topology.ssh.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientChannel.Streaming;
import org.apache.sshd.ClientSession;
import org.apache.sshd.common.util.IoUtils;
import org.slf4j.LoggerFactory;

/**
 * Nested class used to create the client side m_terminal
 * 
 * @author pdgrenon
 * @author lmbell
 */
public class SessionTerminal implements Runnable {
    private final ClientChannel m_channel;  // The connection between the client and the server
    private final Terminal m_terminal;  // The m_terminal to be displayed
    private final PipedOutputStream m_out;  // The output stream to be used by the m_terminal
    private final PipedInputStream m_in;  // The input stream to be used by the m_terminal
    private final Thread m_thread;

    /**
     * Constructor that creates creates the m_terminal and
     * connects the I/O streams to the server
     * @param height 
     * @param width 
     * @param session 
     * @throws IOException
     */
    public SessionTerminal(final ClientSession session, final int width, final int height) throws IOException {
        m_out = new PipedOutputStream();
        m_in = new PipedInputStream();
        m_thread = new Thread(this);
        m_terminal = new Terminal(width, height);
        try {
            m_channel = session.createChannel(ClientChannel.CHANNEL_SHELL);
            m_channel.setIn(m_in);
            m_channel.setOut(m_out);
            m_channel.setErr(m_out);
            m_channel.setStreaming(Streaming.Async);
            m_channel.open().await();
            Thread.sleep(3000);
            m_thread.start();
        } catch (final Exception e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
            stop();
            throw new IOException("Failed to create a channel.", e);
        }
    }


    public void stop() {
        m_channel.close(true);
        IoUtils.closeQuietly(m_out, m_in);
        m_thread.interrupt();
    }


    /**
     * Handles the content received from the server
     * 
     * @param str The content received
     * @param forceDump Whether the m_terminal is forced to dump the content
     * @return The contents dumped to m_terminal
     * @throws IOException
     */
    public String handle(String str, boolean forceDump) throws IOException {
        try {
            if (str != null && str.length() > 0) {
                String d = m_terminal.pipe(str);
                for (byte b : d.getBytes()) {
                    m_out.write(b);
                }
                m_out.flush();
            }
        } catch (IOException e) {
            throw e;
        }
        try {
            return m_terminal.dump();
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
            throw new InterruptedIOException(e.toString());
        }
    }

    /**
     * Runs the m_terminal and reads/writes when necessary
     */
    @Override
    public void run() {
        Reader r = null;
        InputStream is = null;
        try {
            for (;;) {
                byte[] buf = new byte[8192];
                int l = m_in.read(buf);
                is = new ByteArrayInputStream(buf, 0, l);
                r = new InputStreamReader(is);
                final StringBuilder sb = new StringBuilder();
                for (;;) {
                    int c = r.read();
                    if (c == -1) {
                        break;
                    }
                    sb.append((char) c);
                }
                if (sb.length() > 0) {
                    m_terminal.write(sb.toString());
                }
                final String s = m_terminal.read();
                if (s != null && s.length() > 0) {
                    for (final byte b : s.getBytes()) {
                        m_out.write(b);
                    }
                }
            }
        } catch (final IOException e) {
            IoUtils.closeQuietly(r, is);
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
    }

}