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
package org.opennms.core.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The Vault stores application configuration properties.
 *
 * <p>
 * Since our code might be deployed in different environments, this class
 * provides a deployment-neutral way of retrieving configuration properties. 
 * </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public abstract class Vault {

    /**
     * Holds all the application configuration properties.
     */
    private static Properties properties = new Properties(System.getProperties());

    /**
     * Set the application configuration properties.
     *
     * @param properties a {@link java.util.Properties} object.
     */
    public static void setProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Vault.properties = properties;

        /*
         * For backwards compatibility; put all these
         * properties into the system properties.
         */
        Enumeration<Object> keys = properties.keys();
        Properties sysProps = System.getProperties();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            sysProps.put(key, properties.get(key));
        }
    }

    /**
     * Return the entire set of application configuration properties.
     *
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getProperties() {
        return properties;
    }

    /**
     * Return property from the configuration parameter list.
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * <P>
     * Adds new keys to the system properties using the passed key name a the
     * properties location instance. The passed key is used as a key to the
     * system {@link java.util.Properties#getProperty} to find the
     * supplementary property information. The returned value should be in the
     * form of a list of file names, each separated by the system
     * {@link java.io.File#pathSeparator} character.
     * </P>
     *
     * <P>
     * Once the list of files is recovered, each file is visited and loaded into
     * the system properties. If any file cannot be loaded due to an I/O error
     * then it is skipped as a whole. No partial key sets are loaded into the
     * system properties. Also, this method will not overwrite an existing key
     * in the currently loaded properties.
     * </P>
     *
     * @param key
     *            The key name used to lookup the property path values.
     * @return True if all properties loaded correctly, false if any property
     *         file failed to load.
     */
    public static boolean supplementSystemPropertiesFromKey(String key) {
        boolean loadedOK = true;
        String path = System.getProperty(key);
        if (path != null) {
            StringTokenizer files = new StringTokenizer(path, File.pathSeparator);
            while (files.hasMoreTokens()) {
                try {
                    File pfile = new File(files.nextToken());
                    if (pfile.exists() && pfile.isFile()) {
                        Properties p = new Properties();
                        InputStream is = new FileInputStream(pfile);

                        try {
                            p.load(is);
                        } finally {
                            try {
                                is.close();
                            } catch (IOException ioE) { /* do nothing */
                            }
                        }

                        Iterator<Map.Entry<Object,Object>> i = p.entrySet().iterator();
                        while (i.hasNext()) {
                            Map.Entry<Object,Object> e = i.next();
                            if (System.getProperty((String) e.getKey()) == null)
                                System.setProperty((String) e.getKey(), (String) e.getValue());
                        }
                    }
                } catch (IOException ioE) {
                    loadedOK = false;
                }
            } // end while more files to be processed
        } // end if property path no null

        return loadedOK;
    }

}

