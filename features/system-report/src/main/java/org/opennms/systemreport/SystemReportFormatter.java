/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.systemreport;

import java.io.OutputStream;


public interface SystemReportFormatter extends Comparable<SystemReportFormatter> {
    /**
     * A short name for this format for use by UIs when presenting
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
