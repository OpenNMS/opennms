package org.opennms.netmgt.rrd.rrdtool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Rrdcached {
    public static final int DEFAULT_PORT = 42217;

    private Socket m_socket;
    private BufferedReader m_in;
    private PrintWriter m_out;

    public static void main(String[] argv) throws IOException {
        if (argv.length < 3) {
            System.err.println("Incorrect number of command-line arguments.");
            System.err.println("Usage: Rrdcached <host> <port> <command> ...");
            System.exit(1);
        }

        String host = argv[0];
        int port = Integer.parseInt(argv[1]);

        String[] command = new String[argv.length - 2];
        System.arraycopy(argv, 2, command, 0, argv.length - 2);

        Rrdcached r = new Rrdcached(InetAddress.getByName(host), port);

        for (String l : r.execute(command)) {
            System.out.println(l);
        }

        r.quit();
    }

    public Rrdcached(InetAddress host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public Rrdcached(Socket socket) throws IOException {
        m_socket = socket;

        m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        m_out = new PrintWriter(m_socket.getOutputStream());

        testConnection();
    }

    public List<String> execute(String... command) throws IOException {
        StringBuffer b = new StringBuffer();
        for (String c : command) {
            if (b.length() > 0) {
                b.append(" ");
            }
            b.append(c);
        }
        b.append("\n");
        m_out.print(b.toString());
        m_out.flush();

        String r1 = m_in.readLine();
        int space = r1.indexOf(" ");
        if (space == -1) {
            throw new IOException("Could not parse first line of rrdcached response: \"" + r1 + "\""); 
        }
        int code = Integer.parseInt(r1.substring(0, space));
        String message = r1.substring(space + 1);

        if (code < 0) {
            throw new IOException("rrdcached returned an error response (code " + code + "): " + message);
        }

        List<String> data = new ArrayList<String>(code);
        for (int i = 0; i < code; i++) {
            data.add(m_in.readLine());
        }

        return data;
    }

    public void testConnection() throws IOException {
        execute("HELP");
    }

    public void quit() throws IOException {
        m_out.print("QUIT\n");
        m_socket.close();
    }

    public Map<String, Double> getStats() throws IOException {
        List<String> results =  execute("STATS");

        Map<String, Double> stats = new HashMap<String, Double>(results.size());
        for (String l : results) {
            String[] r = l.split(":\\s+");
            if (r.length != 2) {
                throw new IOException("Unparsable statistics line: " + l);
            }

            stats.put(r[0], Double.parseDouble(r[1]));
        }

        return stats;
    }
}
