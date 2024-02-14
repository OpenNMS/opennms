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

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>GroupFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GroupFactory extends GroupManager {
    /**
     * The static singleton instance object
     */
    private static GroupManager s_instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean s_initialized = false;

    /**
     * 
     */
    private File m_groupsConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * Constructor which parses the file
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public GroupFactory() throws FileNotFoundException, IOException {
        super();
        reload();
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException {

        if (s_instance == null || !s_initialized) {
            s_instance = new GroupFactory();
            s_initialized = true;
        }

    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * GroupFactory
     *
     * @return the single group factory instance
     */
    public static synchronized GroupManager getInstance() {
        return s_instance;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param mgr a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public static synchronized void setInstance(GroupManager mgr) {
        s_initialized = true;
        s_instance = mgr;
    }

    /**
     * Parses the groups.xml
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException {
        File confFile = ConfigFileConstants.getFile(ConfigFileConstants.GROUPS_CONF_FILE_NAME);

        reloadFromFile(confFile);
    }

    /**
     * @param confFile
     * @throws IOException
     */
    private void reloadFromFile(File confFile) throws IOException {
        m_groupsConfFile = confFile;
        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_groupsConfFile);
            m_lastModified = m_groupsConfFile.lastModified();
            parseXml(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void saveXml(String data) throws IOException {
        if (data != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_groupsConfFile), StandardCharsets.UTF_8);
            fileWriter.write(data);
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public void update() throws IOException {
        if (m_lastModified != m_groupsConfFile.lastModified()) {
            reload();
        }
    }
}
