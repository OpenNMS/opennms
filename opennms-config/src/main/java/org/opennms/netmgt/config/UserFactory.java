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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;

/**
 * <p>UserFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class UserFactory extends UserManager {
    private static final long RELOAD_CHECK_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * The static singleton instance of the UserFactory
     */
    private static UserManager instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;

    /**
     * 
     */
    private File m_usersConfFile;

    /**
     * 
     */
    private long m_lastModified;

    /**
     * 
     */
    private long m_fileSize;

    private AtomicLong m_lastReloadCheck = new AtomicLong(0);

    /**
     * Initializes the factory
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public UserFactory() throws  FileNotFoundException, IOException {
        super(GroupFactory.getInstance());
        reload();
    }

    /**
     * <p>init</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public static synchronized void init() throws IOException, FileNotFoundException {
        
        if (instance == null || !initialized) {
            GroupFactory.init();
            instance = new UserFactory();
            initialized = true;
        }

    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * UserFactory
     *
     * @return the single user factory instance
     */
    public static synchronized UserManager getInstance() {
        return instance;
    }
    
    /**
     * <p>Setter for the field <code>instance</code>.</p>
     *
     * @param mgr a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public static synchronized void setInstance(UserManager mgr) {
        initialized = true;
        instance = mgr;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    public synchronized void reload() throws IOException, FileNotFoundException {
        // Form the complete filename for the config file
        //
        m_usersConfFile = ConfigFileConstants.getFile(ConfigFileConstants.USERS_CONF_FILE_NAME);

        InputStream configIn = new FileInputStream(m_usersConfFile);
        m_lastModified = m_usersConfFile.lastModified();
        m_fileSize = m_usersConfFile.length();

        parseXML(configIn);
        
        initialized = true;

    }

    /** {@inheritDoc} */
    @Override
    protected void saveXML(String writerString) throws IOException {
        if (writerString != null) {
            Writer fileWriter = null;
            try {
                fileWriter = new OutputStreamWriter(new FileOutputStream(m_usersConfFile), StandardCharsets.UTF_8);
                fileWriter.write(writerString);
                fileWriter.flush();
            } finally {
                IOUtils.closeQuietly(fileWriter);
            }
        }
    }

    /**
     * <p>isUpdateNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isUpdateNeeded() {
        if (m_usersConfFile == null) {
            return true;
        }

        final long now = System.currentTimeMillis();
        if (now < (m_lastReloadCheck.get() + RELOAD_CHECK_INTERVAL_MS)) {
            return false;
        }
        m_lastReloadCheck.set(now);

        synchronized (this) {
            final long fileLastModified = m_usersConfFile.lastModified();

            // Check to see if the file size has changed
            if (m_fileSize != m_usersConfFile.length()) {
                return true;
                // Check to see if the timestamp has changed
            } else if (m_lastModified != fileLastModified) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws java.io.FileNotFoundException if any.
     */
    @Override
    public void doUpdate() throws IOException, FileNotFoundException {
        if (isUpdateNeeded()) {
            reload();
        }
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    @Override
    public long getFileSize() {
        return m_fileSize;
    }
}
