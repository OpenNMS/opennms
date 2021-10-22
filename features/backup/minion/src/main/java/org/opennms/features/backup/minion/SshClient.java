package org.opennms.features.backup.minion;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SshClient.class);
    public static final int DEFAULT_TIMEOUT_MS = 5*1000;

    private final JSch jsch = new JSch();
    private Session session;
    private Channel channel;
    private PrintStream stdin;
    private InputStream stdout;
    private InputStream stderr;

    private final StringBuffer stdoutBuff = new StringBuffer();
    private final StringBuffer stderrBuff = new StringBuffer();

    private final InetSocketAddress addr;
    private final String username;
    private final String password;

    private int timeout = DEFAULT_TIMEOUT_MS;

    public SshClient(InetSocketAddress addr, String username, String password) {
        this.addr = Objects.requireNonNull(addr);
        this.username = username;
        this.password = password;
    }

    public PrintStream openShell() throws Exception {
        // We only support one shell at a time
        close();

        session = jsch.getSession(username, addr.getHostString(), addr.getPort());
        String keyFile = createKeyFile(password);
        jsch.addIdentity(keyFile);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        channel = session.openChannel("shell");
        stdout = channel.getInputStream();
        stderr = channel.getExtInputStream();
        channel.connect(timeout);

        OutputStream ops = channel.getOutputStream();
        stdin = new PrintStream(ops, true);
        new File(keyFile).delete();

        return stdin;
    }

    private String createKeyFile(String keyStr){
        String keyFileName = "key";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(keyFileName));
            writer.write(keyStr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyFileName;
    }

    public String getStdout() throws IOException {
        // Prepend the contents of the buffer, which may be have populated by isShellClosed()
        final String stdoutContents = stdoutBuff.toString() + readAvailableBytes(stdout);
        stdoutBuff.setLength(0);
        return stdoutContents;
    }

    public String getStderr() throws IOException {
        // Prepend the contents of the buffer, which may be have populated by isShellClosed()
        final String stderrContents = stderrBuff.toString() + readAvailableBytes(stderr);
        stderrBuff.setLength(0);
        return stderrContents;
    }

    @Override
    public void close(){
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    /**
     * Checks if the shell's channel is closed.
     *
     * Can be used to make sure that stdout/stderr get fully
     * populated after an exit/logout command is issued
     * in the shell.
     */
    public boolean isShellClosed() {
        if (channel == null) {
            return true;
        }

        // In certain cases the shell won't close unless we read the available
        // bytes from the stdout and stderr streams.
        try {
            stdoutBuff.append(getStdout());
        } catch (IOException e) {
            // pass
        }

        try {
            stderrBuff.append(getStderr());
        } catch (IOException e) {
            // pass
        }

        return channel.isClosed();
    }

    /**
     * Read all of the available bytes on the given stream and converts them
     * to a string.
     *
     * Note that this may cause problems if a multi-byte character is not
     * completely read.
     */
    private static String readAvailableBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int BUF_LEN = 1024;
        final byte[] buffer = new byte[BUF_LEN];
        int avail = 0;
        while ((avail = is.available()) > 0) {
            int length = is.read(buffer, 0, Math.min(BUF_LEN, avail));
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    public Callable<Boolean> isShellClosedCallable() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isShellClosed();
            }
        };
    }

    public static class Streams {
        public final PrintStream stdin;
        public final StreamGobbler stdout, stderr;
        public Streams(PrintStream stdin, StreamGobbler stdout, StreamGobbler stderr) {
            this.stdin = stdin;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
