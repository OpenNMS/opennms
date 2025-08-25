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

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.menu.MainMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MenuTemplateFactory extends MenuTemplateManager {
    private static final Logger LOG = LoggerFactory.getLogger(MenuTemplateFactory.class);

    private static final long RELOAD_CHECK_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5);

    private static MenuTemplateManager instance;

    /**
     * Boolean indicating if the init() method has been called
     */
    private static boolean initialized = false;
    private File menuTemplateConfFile;
    private long lastModified;
    private long fileSize;
    private ObjectMapper objectMapper = new ObjectMapper();

    private AtomicLong lastReloadCheck = new AtomicLong(0);
    private String menuTemplateFilePath; // used in tests
    private MainMenu mainMenu;

    public MenuTemplateFactory() throws IOException {
        update();
    }

    public MenuTemplateFactory(String menuTemplateFilePath) throws IOException {
        this.menuTemplateFilePath = menuTemplateFilePath;
        update();
    }

    public static synchronized void init(String menuTemplateFilePath) throws IOException, FileNotFoundException {
        if (instance == null || !initialized) {
            instance = menuTemplateFilePath != null ? new MenuTemplateFactory(menuTemplateFilePath) : new MenuTemplateFactory();
            initialized = true;
        }
    }

    public static synchronized void init() throws IOException, FileNotFoundException {
        init(null);
    }

    /**
     * Singleton static call to get the only instance that should exist for the
     * MenuTemplateFactory.
     *
     * @return the single menu template factory instance.
     */
    public static synchronized MenuTemplateManager getInstance() {
        return instance;
    }

    public static synchronized void setInstance(MenuTemplateManager mgr) {
        initialized = true;
        instance = mgr;
    }

    public synchronized void reload() throws IOException, FileNotFoundException {
        if (menuTemplateFilePath != null && !menuTemplateFilePath.isEmpty()) {
            menuTemplateConfFile = new File(menuTemplateFilePath);
        } else {
            menuTemplateConfFile = ConfigFileConstants.getFile(ConfigFileConstants.MENU_TEMPLATE_FILE_NAME);
        }

        LOG.debug("MenuTemplateFactory: reload, using menu template file: {}", menuTemplateConfFile.getAbsolutePath());

        lastModified = menuTemplateConfFile.lastModified();
        fileSize = menuTemplateConfFile.length();

        mainMenu = parseMenuTemplate(menuTemplateConfFile);

        initialized = true;
    }

    @Override
    public boolean isUpdateNeeded() {
        if (menuTemplateConfFile == null) {
            return true;
        }

        final long now = System.currentTimeMillis();
        if (now < (lastReloadCheck.get() + RELOAD_CHECK_INTERVAL_MS)) {
            return false;
        }
        lastReloadCheck.set(now);

        synchronized (this) {
            final long fileLastModified = menuTemplateConfFile.lastModified();

            // Check to see if the file size has changed
            if (fileSize != menuTemplateConfFile.length()) {
                return true;
            // Check to see if the timestamp has changed
            } else if (lastModified != fileLastModified) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void doUpdate() throws IOException, FileNotFoundException {
        if (isUpdateNeeded()) {
            reload();
        }
    }

    @Override
    public MainMenu cloneMainMenu() throws IOException {
        update();

        return objectMapper.treeToValue(objectMapper.valueToTree(mainMenu), MainMenu.class);
    }

    private MainMenu parseMenuTemplate(File file) {
        MainMenu template = null;

        try (var fis = new FileInputStream(file)) {
            template = objectMapper.readValue(fis, MainMenu.class);
        } catch (FileNotFoundException fnfe) {
            LOG.error("ERROR parsing menu template: FileNotFoundException: {}", fnfe.getMessage());
            return null;
        } catch (DatabindException dbex) {
            LOG.error("ERROR parsing menu template: DatabindException: {}", dbex.getMessage());
            return null;
        } catch (Exception e) {
            LOG.error("ERROR parsing menu template: Exception: {}", e.getMessage());
            return null;
        }

        return template;
    }
}
