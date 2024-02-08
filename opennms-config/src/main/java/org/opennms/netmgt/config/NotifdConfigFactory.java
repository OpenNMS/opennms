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
 * <p>NotifdConfigFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NotifdConfigFactory extends NotifdConfigManager {
    /**
     * Singleton instance
     */
    private static NotifdConfigFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_notifdConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private NotifdConfigFactory() {
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotifdConfigFactory} object.
     */
    static synchronized public NotifdConfigFactory getInstance() {
        if (!initialized)
            throw new IllegalStateException("init() not called.");

        return instance;
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException {
        if (!initialized) {
            instance = new NotifdConfigFactory();
            instance.reload();
            initialized = true;
        }
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException {
        m_notifdConfFile = ConfigFileConstants.getFile(ConfigFileConstants.NOTIFD_CONFIG_FILE_NAME);

        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_notifdConfFile);
            m_lastModified = m_notifdConfFile.lastModified();
            parseXml(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /**
     * Gets a nicely formatted string for the Web UI to display
     *
     * @return On, Off, or Unknown depending on status
     *
     * TODO: Pull up into base class but keep this reference for the
     * webapp until singleton is removed.
     * @throws java.io.IOException if any.
     */
    public static String getPrettyStatus() throws IOException {
        if (!initialized)
            return "Unknown";

        String status = "Unknown";

        status = NotifdConfigFactory.getInstance().getNotificationStatus();

        if (status.equals("on"))
            status = "On";
        else if (status.equals("off"))
            status = "Off";

        return status;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_notifdConfFile), StandardCharsets.UTF_8);
            fileWriter.write(xml);
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
    protected synchronized void update() throws IOException {
        if (m_lastModified != m_notifdConfFile.lastModified()) {
            NotifdConfigFactory.getInstance().reload();
        }
    }

}
