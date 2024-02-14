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
package org.opennms.systemreport.formatters;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.core.io.Resource;

public class FullTextSystemReportFormatter extends AbstractSystemReportFormatter implements SystemReportFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(FullTextSystemReportFormatter.class);
    @Override
    public String getName() {
        return "full";
    }

    @Override
    public String getDescription() {
        return "Human-readable text (full output)";
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public String getExtension() {
        return "txt";
    }

    @Override
    public boolean canStdout() {
        return true;
    }

    @Override
    public void write(final SystemReportPlugin plugin) {
        final OutputStream out = getOutputStream();

        try {
            out.write(String.format("= %s: %s =%n%n", plugin.getName(), plugin.getDescription()).getBytes());
            
            for (final Map.Entry<String,Resource> entry : plugin.getEntries().entrySet()) {
                final Resource value = entry.getValue();

                out.write(String.format("== %s ==%n%n", entry.getKey()).getBytes());

                final InputStream is = value.getInputStream();
                int bytes;
                byte[] buffer = new byte[1024];
                
                while ((bytes = is.read(buffer)) != -1) {
                    out.write(buffer, 0, bytes);
                }
                is.close();

                out.write(String.format("%n%n").getBytes());
            }

        } catch (final Exception e) {
            LOG.info("unable to write", e);
        }
    }

}
