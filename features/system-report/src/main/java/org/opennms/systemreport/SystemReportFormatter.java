package org.opennms.systemreport;

import java.io.OutputStream;


public interface SystemReportFormatter extends Comparable<SystemReportFormatter> {
    /**
     * A short name for this format for use by UI's when presenting
     * an option of which formatter to choose.
     * @return the format name (eg, "text", "xml", etc.)
     */
    public String getName();

    /**
     * A short description of the format.
     * @return the description
     */
    public String getDescription();

    /**
     * The Content-Type that this formatter outputs.  This can be null
     * if the formatter does not produce a file (like the FtpSystemReportFormatter).
     * @return the content-type the formatter writes.
     */
    public String getContentType();

    /**
     * The default extension to use when creating files if no output
     * is specified.
     * @return the extension
     */
    public String getExtension();

    /**
     * Whether or not this formatter needs to be given an output stream.
     */
    public boolean needsOutputStream();

    /**
     * The output string as passed on the command-line.
     */
    public void setOutput(final String output);

    /**
     * The output stream to use when writing data.
     * @param stream
     */
    public void setOutputStream(final OutputStream stream);

    /**
     * Write the data from the specified {@link SystemReportPlugin}.
     * @param plugin the system report plugin which contains the data to write
     */
    public void write(final SystemReportPlugin plugin);

    /**
     * Indicates that report output will begin.
     */
    public void begin();

    /**
     * Indicates that report output will end.
     */
    public void end();

    /**
     * Whether this formatter should be allowed to write to STDOUT.
     */
    public boolean canStdout();

}
