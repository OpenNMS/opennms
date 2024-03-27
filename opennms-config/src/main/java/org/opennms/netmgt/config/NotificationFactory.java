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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>NotificationFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NotificationFactory extends NotificationManager {
    /**
     * Singleton instance
     */
    private static NotificationFactory instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_noticeConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private NotificationFactory() {
        super(NotifdConfigFactory.getInstance(), DataSourceFactory.getInstance());
    }

    /**
     * <p>Getter for the field <code>instance</code>.</p>
     *
     * @return a {@link org.opennms.netmgt.config.NotificationFactory} object.
     */
    static synchronized public NotificationFactory getInstance() {
        if (!initialized)
            return null;

        return instance;
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.sql.SQLException if any.
     * @throws java.beans.PropertyVetoException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException, ClassNotFoundException, SQLException, PropertyVetoException  {
        if (!initialized) {
            instance = new NotificationFactory();
            instance.reload();
            initialized = true;
        }
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void reload() throws IOException {
        m_noticeConfFile = ConfigFileConstants.getFile(ConfigFileConstants.NOTIFICATIONS_CONF_FILE_NAME);

        InputStream configIn = null;
        try {
            configIn = new FileInputStream(m_noticeConfFile);
            m_lastModified = m_noticeConfFile.lastModified();
            parseXML(configIn);
        } finally {
            if (configIn != null) {
                IOUtils.closeQuietly(configIn);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void saveXML(String xmlString) throws IOException {
        if (xmlString != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(m_noticeConfFile), StandardCharsets.UTF_8);
            fileWriter.write(xmlString);
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
        if (m_lastModified != m_noticeConfFile.lastModified()) {
            reload();
        }
    }
}
