package org.opennms.install;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class ProcessExec {
    PrintStream m_out = null;
    PrintStream m_err = null;

    public ProcessExec(PrintStream out, PrintStream err) {
	m_out = out;
	m_err = err;
    }

    public int exec(String[] cmd) throws IOException, InterruptedException {
	Process p = Runtime.getRuntime().exec(cmd);

	p.getOutputStream().close();
	PrintInputStream out = new PrintInputStream(p.getInputStream(), m_out);
	PrintInputStream err = new PrintInputStream(p.getErrorStream(), m_err);

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
		BufferedReader in =
		    new BufferedReader(new InputStreamReader(m_inputStream));
		String line;

		while ((line = in.readLine()) != null) {
		    m_printStream.println(line);
		}
	
		m_inputStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
		try {
		    m_inputStream.close();
		} catch (IOException e2) { } // do nothing
	    }
	}

    }
}