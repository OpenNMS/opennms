package org.opennms.install;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class ProcessExec {

    public ProcessExec() {
    }

    public int exec(String[] cmd) throws IOException, InterruptedException {
	Process p = Runtime.getRuntime().exec(cmd);

	PrintInputStream out = new PrintInputStream(p.getInputStream(),
						    System.out);
	PrintInputStream err = new PrintInputStream(p.getErrorStream(),
						    System.err);

	Thread t1 = new Thread(out);
	Thread t2 = new Thread(err);
	t1.start();
	t2.start();

	int exitVal = p.waitFor();

	t1.join();
	t2.join();

	return exitVal;
    }

    public class PrintInputStream implements Runnable {
	private InputStream m_inputStream;
	private PrintStream m_printStream;

	public PrintInputStream(InputStream inputStream,
				PrintStream printStream) {
	    m_inputStream = inputStream;
	    m_printStream = printStream;
	}

	public void run() {
	    try {
		runCatch();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	private void runCatch() throws IOException {
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(m_inputStream));
	    String line;

	    while ((line = in.readLine()) != null) {
		m_printStream.println(line);
	    }
	}
    }
}