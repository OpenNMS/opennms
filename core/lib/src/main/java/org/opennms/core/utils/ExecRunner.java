/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * This file is a derivative work, containing both original code, included code,
 * and modified code that was published under the GNU General Public License.
 * 
 * Original code for this file Copyright (C) 2002 Scott McCrory.
 * All rights reserved.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.core.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;

/**
 * <P>
 * Runs external executables, optionally under a watched thread.
 *
 * In addition, probably the most useful feature of ExecRunner is using it to
 * run a command-line program and obtain its stdout and stderr results in two
 * strings. This is done with exec(String) - see that method for an example.
 *
 * With acknowledgements to Michael C. Daconta, author of "Java Pitfalls, Time
 * Saving Solutions, and Workarounds to Improve Programs." and his article in
 * JavaWorld "When Runtime.exec() Won't".
 * </P>
 *
 * @author <a href="mailto:smccrory@users.sourceforge.net">Scott McCrory </a>.
 */
public class ExecRunner {

    /** Win NT/2K/MEPro require cmd.exe to run programs * */
    private static final String WINDOWS_NT_2000_COMMAND_1 = "cmd.exe";

    /** Win NT/2K/MEPro require the /C to specify what to run * */
    private static final String WINDOWS_NT_2000_COMMAND_2 = "/C";

    /** Win 9X/MEHome require cmd.exe to run programs * */
    private static final String WINDOWS_9X_ME_COMMAND_1 = "command.exe";

    /** Win 9X/MEHome require the /C to specify what to run * */
    private static final String WINDOWS_9X_ME_COMMAND_2 = "/C";

    /** String to send to STDERR if program exceeds max run time * */
    private static final String MAX_RUN_TIME_EXCEEDED_STRING = "MAX_RUN_TIME_EXCEEDED";

    /** String to capture STDOUT */
    private String out = "".intern();

    /** String to capture STDERR */
    private String err = "".intern();

    /** Default max run time (in seconds) */
    private int maxRunTimeSecs = 0;

    /** Flag to indicate if we've exceeded max run time */
    private boolean maxRunTimeExceeded = false;

    /** Number of milliseconds to wait between polling watched thread */
    private static final int POLL_DELAY_MS = 100;

    /**
     * Basic ExecRunner constructor.
     */
    public ExecRunner() {
        super();
    }

    /**
     * ExecRunner constructor which also conveniently runs exec(String).
     *
     * @param command
     *            The program or command to run
     * @throws java.lang.ExceptionInInitializerError
     *             thrown if a problem occurs
     */
    public ExecRunner(final String command) throws ExceptionInInitializerError {
        this();
        try {
            exec(command);
        } catch (final Throwable e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }

    }

    /**
     * We override the <code>clone</code> method here to prevent cloning of
     * our class.
     *
     * @throws java.lang.CloneNotSupportedException
     *             To indicate cloning is not allowed
     * @return Nothing ever really returned since we throw a
     *         CloneNotSupportedException
     */
    @Override
    public final Object clone() throws CloneNotSupportedException {

        throw new java.lang.CloneNotSupportedException();

    }

    /**
     * The <B>exec(String) </B> method runs a process inside of a watched
     * thread. It returns the client's exit code and feeds its STDOUT and STDERR
     * to ExecRunner's out and err strings, where you then use getOutString()
     * and getErrString() to obtain these values. Example:
     *
     * <pre>
     *
     * // Execute the program and grab the results
     * try {
     *     ExecRunner er = new ExecRunner();
     *     er.setMaxRunTimeSecs(5);
     *     er.exec(&quot;ls -l&quot;);
     *     if (!er.getMaxRunTimeExceeded()) {
     *         out = er.getOutString();
     *         err = er.getErrString();
     *     } else {
     *         System.out.println(&quot;Maximum run time exceeded!&quot;);
     *     }
     * } catch (Throwable e) {
     *     System.out.println(&quot;Error executing &quot; + program + &quot;: &quot; + e.getMessage());
     *     continue;
     * }
     * </pre>
     *
     * @return The command's return code
     * @param command
     *            The program or command to run
     * @throws java.io.IOException
     *             thrown if a problem occurs
     * @throws java.lang.InterruptedException
     *             thrown if a problem occurs
     */
    public int exec(final String command) throws IOException, InterruptedException {

        StringWriter swOut = null;
        PrintWriter pwOut = null;

        StringWriter swErr = null;
        PrintWriter pwErr = null;

        try {
            swOut = new StringWriter();
            pwOut = new PrintWriter(swOut, true);

            swErr = new StringWriter();
            pwErr = new PrintWriter(swErr, true);

            int rc = exec(command, pwOut, pwErr);

            out = swOut.toString();
            err = swErr.toString();

            return rc;
        } finally {
            IOUtils.closeQuietly(pwErr);
            IOUtils.closeQuietly(swErr);
            IOUtils.closeQuietly(pwOut);
            IOUtils.closeQuietly(swOut);
        }
    }

    /**
     * Convenience method for calling exec with OutputStreams.
     *
     * @return The command's return code
     * @param command
     *            The program or command to run
     * @param stdoutStream
     *            java.io.OutputStream
     * @param stderrStream
     *            java.io.OutputStream
     * @throws java.io.IOException
     *             thrown if a problem occurs
     * @throws java.lang.InterruptedException
     *             thrown if a problem occurs
     */
    public int exec(final String command, final OutputStream stdoutStream, final OutputStream stderrStream) throws IOException, InterruptedException {

        PrintWriter pwOut = null;
        PrintWriter pwErr = null;

        try {
            pwOut = new PrintWriter(stdoutStream, true);
            pwErr = new PrintWriter(stderrStream, true);

            return exec(command, pwOut, pwErr);
        } finally {
            IOUtils.closeQuietly(pwErr);
            IOUtils.closeQuietly(pwOut);
        }
    }

    /**
     * The <code>exec(String, PrintWriter, PrintWriter)</code> method runs a
     * process inside of a watched thread. It returns the client's exit code and
     * feeds its STDOUT and STDERR to the passed-in streams.
     *
     * @return The command's return code
     * @param command
     *            The program or command to run
     * @param stdoutWriter
     *            java.io.PrintWriter
     * @param stderrWriter
     *            java.io.PrintWriter
     * @throws java.io.IOException
     *             thrown if a problem occurs
     * @throws java.lang.InterruptedException
     *             thrown if a problem occurs
     */
    public int exec(final String command, final PrintWriter stdoutWriter, final PrintWriter stderrWriter) throws IOException, InterruptedException {

        // Default exit value is non-zero to indicate a problem.
        int exitVal = 1;

        // //////////////////////////////////////////////////////////////
        final Runtime rt = Runtime.getRuntime();
        Process proc;
        String[] cmd = null;

        // First get the start time & calculate comparison numbers
        final Date startTime = new Date();
        final long startTimeMs = startTime.getTime();
        final long maxTimeMs = startTimeMs + (maxRunTimeSecs * 1000);

        // //////////////////////////////////////////////////////////////
        // First determine the OS to build the right command string
        final String osName = System.getProperty("os.name");
        if (osName.equals("Windows 95") || osName.equals("Windows 98") || osName.equals("Windows ME")) {
            cmd = new String[3];
            cmd[0] = WINDOWS_9X_ME_COMMAND_1;
            cmd[1] = WINDOWS_9X_ME_COMMAND_2;
            cmd[2] = command;
        } else if (osName.contains("Windows")) { // "Windows NT", "Windows 2000", etc.
            cmd = new String[3];
            cmd[0] = WINDOWS_NT_2000_COMMAND_1;
            cmd[1] = WINDOWS_NT_2000_COMMAND_2;
            cmd[2] = command;
        } else {
            // Linux (and probably other *nixes) prefers to be called
            // with each argument supplied separately, so we first
            // Tokenize it across spaces as the boundary.
            final StringTokenizer st = new StringTokenizer(command, " ");
            cmd = new String[st.countTokens()];
            int token = 0;
            while (st.hasMoreTokens()) {
                String tokenString = st.nextToken();
                cmd[token++] = tokenString;
            }
        }

        // Execute the command and start the two output gobblers
        if (cmd != null && cmd.length > 0) {
            proc = rt.exec(cmd);
        } else {
            throw new IOException("Insufficient commands!");
        }

        final StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), stdoutWriter);
        final StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), stderrWriter);
        outputGobbler.start();
        errorGobbler.start();

        // Wait for the program to finish running and return the
        // exit value obtained from the executable
        while (true) {

            try {
                exitVal = proc.exitValue();
                break;
            } catch (final IllegalThreadStateException e) {

                // If we get this exception, then the process isn't
                // done executing and we determine if our time is up.
                if (maxRunTimeSecs > 0) {

                    final Date endTime = new Date();
                    final long endTimeMs = endTime.getTime();
                    if (endTimeMs > maxTimeMs) {
                        // Time's up - kill the process and the gobblers and
                        // return
                        proc.destroy();
                        maxRunTimeExceeded = true;
                        stderrWriter.println(MAX_RUN_TIME_EXCEEDED_STRING);
                        outputGobbler.quit();
                        errorGobbler.quit();
                        return exitVal;

                    } else {
                        // Time is not up yet so wait 100 ms before testing
                        // again
                        Thread.sleep(POLL_DELAY_MS);
                    }

                }

            }

        }

        // //////////////////////////////////////////////////////////////
        // Wait for output gobblers to finish forwarding the output
        while (outputGobbler.isAlive() || errorGobbler.isAlive()) {
        }

        // //////////////////////////////////////////////////////////////
        // All done, flush the streams and return the exit value
        stdoutWriter.flush();
        stderrWriter.flush();
        return exitVal;

    }

    /**
     * Returns the error string if exec(String) was invoked.
     *
     * @return The error string if exec(String) was invoked.
     */
    public String getErrString() {
        return err;
    }

    /**
     * Returns whether the maximum runtime was exceeded or not.
     *
     * @return boolean indicating whether the maximum runtime was exceeded or
     *         not.
     */
    public boolean isMaxRunTimeExceeded() {
        return maxRunTimeExceeded;
    }

    /**
     * Returns the maximum run time in seconds for this object.
     *
     * @return the maximum run time in seconds for this object.
     */
    public int getMaxRunTimeSecs() {
        return maxRunTimeSecs;
    }

    /**
     * Returns the output string if exec(String) was invoked.
     *
     * @return The output string if exec(String) was invoked.
     */
    public String getOutString() {
        return out;
    }

    /**
     * This is for unit testing of the class.
     *
     * @param args
     *            an array of command-line arguments
     * @throws java.io.IOException
     *             thrown if a problem occurs
     */
    public static void main(final String[] args) throws IOException {

        try {

            final ExecRunner er = new ExecRunner();

            // ///////////////////////////////////////////////////////////////////
            // Windows: Test the exec operation with just STDOUT and STDERR
            System.out.println("Testing ExecRunner with StringWriter...");

            er.setMaxRunTimeSecs(1);
            er.exec("dir /s c:\\");
            // er.exec("ls -l");

            System.out.println("<STDOUT>\n" + er.getOutString() + "</STDOUT>");
            System.out.println("<STDERR>\n" + er.getErrString() + "</STDERR>");
            System.out.println("Testing Done");

            // ///////////////////////////////////////////////////////////////////
            // Exit nicely
            System.exit(0);

        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

    /**
     * We override the <code>readObject</code> method here to prevent
     * deserialization of our class for security reasons.
     * 
     * @param in
     *            java.io.ObjectInputStream
     * @throws IOException
     *             thrown if a problem occurs
     */
    private final void readObject(final ObjectInputStream in) throws IOException {

        throw new IOException("Object cannot be deserialized");

    }

    /**
     * Sets the maximum run time in seconds. If you do not want to limit the
     * executable's run time, simply pass in a 0 (which is also the default).
     *
     * @param max
     *            Maximim number of seconds to let program run
     */
    public void setMaxRunTimeSecs(final int max) {

        maxRunTimeSecs = max;

    }

    /**
     * We override the <code>writeObject</code> method here to prevent
     * serialization of our class for security reasons.
     * 
     * @param out
     *            java.io.ObjectOutputStream
     * @throws IOException
     *             thrown if a problem occurs
     */
    private final void writeObject(final ObjectOutputStream out) throws IOException {

        throw new IOException("Object cannot be serialized");

    }

}
