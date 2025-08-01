/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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


    public boolean isVisible();

}
