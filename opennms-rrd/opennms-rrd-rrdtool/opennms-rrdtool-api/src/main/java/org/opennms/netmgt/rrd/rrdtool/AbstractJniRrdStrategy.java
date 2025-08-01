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
package org.opennms.netmgt.rrd.rrdtool;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdGraphDetails;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

public abstract class AbstractJniRrdStrategy<D extends Object,F extends Object> implements RrdStrategy<D, F> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJniRrdStrategy.class);

	private static final String IGNORABLE_LIBART_WARNING_STRING = "*** attempt to put segment in horiz list twice";
	private static final String IGNORABLE_LIBART_WARNING_REGEX = "\\*\\*\\* attempt to put segment in horiz list twice\r?\n?";

    /**
     * {@inheritDoc}
     *
     * Executes the given graph command as process with workDir as the current
     * directory. The output stream of the command (a PNG image) is copied to a
     * the InputStream returned from the method.
     */
    @Override
    public InputStream createGraph(String command, File workDir) throws IOException, RrdException {
        byte[] byteArray = createGraphAsByteArray(command, workDir);
        return new ByteArrayInputStream(byteArray);
    }

    private byte[] createGraphAsByteArray(String command, File workDir) throws IOException, RrdException {
        String[] commandArray = StringUtils.createCommandArray(command);
        Process process;
        try {
             process = Runtime.getRuntime().exec(commandArray, null, workDir);
        } catch (IOException e) {
            IOException newE = new IOException("IOException thrown while executing command '" + command + "' in " + workDir.getAbsolutePath() + ": " + e);
            newE.initCause(e);
            throw newE;
        }
        
        // this closes the stream when its finished
        byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());
        
        // this close the stream when its finished
        String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
        
        // one particular warning message that originates in libart should be ignored
        if (errors.length() > 0 && errors.contains(IGNORABLE_LIBART_WARNING_STRING)) {
        	LOG.debug("Ignoring libart warning message in rrdtool stderr stream: {}", IGNORABLE_LIBART_WARNING_STRING);
        	errors = errors.replaceAll(IGNORABLE_LIBART_WARNING_REGEX, "");
        }
        if (errors.length() > 0) {
            throw new RrdException(errors);
        }
        return byteArray;
    }

    // These offsets work perfectly for ranger@ with rrdtool 1.2.23 and Firefox
    /**
     * <p>getGraphLeftOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphLeftOffset() {
        return 65;
    }

    /**
     * <p>getGraphRightOffset</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphRightOffset() {
        return -30;
    }

    /**
     * <p>getGraphTopOffsetWithText</p>
     *
     * @return a int.
     */
    @Override
    public int getGraphTopOffsetWithText() {
        return -75;
    }

    /**
     * No stats are kept for this implementation.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStats() {
        return "";
    }

    /**
     * <p>getDefaultFileExtension</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getDefaultFileExtension() {
        return ".rrd";
    }
    
    /** {@inheritDoc} */
    @Override
    public RrdGraphDetails createGraphReturnDetails(String command, File workDir) throws IOException, org.opennms.netmgt.rrd.RrdException {
        // Creating Temp PNG File
        File pngFile = File.createTempFile("opennms.rrdtool.", ".png");
        command = command.replaceFirst("graph - ", "graph " + pngFile.getAbsolutePath() + " ");

        int width;
        int height;
        String[] printLines;
        InputStream pngStream;

        try {
            // Executing RRD Command
            InputStream is = createGraph(command, workDir);
            
            // Processing Command Output
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            try {
                String line = null;
                if ((line = reader.readLine()) == null) {
                    throw new IOException("No output from the createGraph() command");
                }
                String[] s = line.split("x");
                width = Integer.parseInt(s[0]);
                height = Integer.parseInt(s[1]);
                
                List<String> printLinesList = new ArrayList<>();
                
                while ((line = reader.readLine()) != null) {
                    printLinesList.add(line);
                }
                
                printLines = printLinesList.toArray(new String[printLinesList.size()]);

            } finally {
                reader.close();
            }

            // Creating PNG InputStream
            byte[] byteArray = FileCopyUtils.copyToByteArray(pngFile);
            pngStream = new ByteArrayInputStream(byteArray);
        } catch (Throwable e) {
            throw new RrdException("Can't execute command " + command, e);
        } finally {
            if (!pngFile.delete()) {
            	LOG.warn("Could not delete file: {}", pngFile.getPath());
            }
        }

        // Creating Graph Details
        return new JniGraphDetails(width, height, printLines, pngStream);
    }

    /** {@inheritDoc} */
    @Override
    public void promoteEnqueuedFiles(Collection<String> rrdFiles) {
        // no need to do anything since this strategy doesn't queue
    }
}
