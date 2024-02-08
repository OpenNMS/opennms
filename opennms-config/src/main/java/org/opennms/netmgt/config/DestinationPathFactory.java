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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>DestinationPathFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DestinationPathFactory extends DestinationPathManager {
    /**
     * 
     */
    private static DestinationPathFactory instance;

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static File m_notifConfFile;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private static File m_pathsConfFile;

    /**
     * 
     */
    private static long m_lastModified;

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException {
        if (!initialized) {
            getInstance().reload();
            initialized = true;
        }
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.DestinationPathFactory} object.
     */
    public static synchronized DestinationPathFactory getInstance() {

        if (instance == null || !initialized) {
            instance = new DestinationPathFactory();
        }

        return instance;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public void reload() throws IOException, FileNotFoundException {
        m_pathsConfFile = ConfigFileConstants.getFile(ConfigFileConstants.DESTINATION_PATHS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_pathsConfFile);
        m_lastModified = m_pathsConfFile.lastModified();

        parseXML(configIn);
        configIn.close();
    }

    /** {@inheritDoc} */
    @Override
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_pathsConfFile), StandardCharsets.UTF_8);
            fileWriter.write(writerString);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    @Override
    public void update() throws IOException, FileNotFoundException {
        // see NMS-16151
        if (!initialized) {
            init();
        }

        if (m_lastModified != m_pathsConfFile.lastModified()) {
            reload();
        }
    }
}
