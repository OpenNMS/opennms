//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//
//

package org.opennms.core.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.opennms.core.resource.db.DbConnectionFactory;

/**
 * The Vault handles policies for allocating/deallocating scarce resources and
 * stores application configuration properties.
 * 
 * <p>
 * Since our code might be deployed in different environments, this class
 * provides a deployment-neutral way of retrieving scarce resources and
 * configuration properties. Our application code may be deployed as a daemon,
 * command-line interface, graphical user interface, applet, or servlet. Some of
 * the scarce resources we might like to have allocation policies for are
 * database connections, socket connections, RMI connections, CORBA connections,
 * or open temporary files.
 * </p>
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class Vault extends Object {

    /**
     * Holds all the application configuration properties.
     */
    protected static Properties properties = new Properties(System.getProperties());

    /**
     * Stores the directory where the OpenNMS configuration files can be found.
     * The default is <em>/opt/OpenNMS</em>.
     */
    protected static String homeDir = "/opt/opennms/";

    /**
     * A delegate object that encapsulates the JDBC database connection
     * allocation management. For example, some factories might preallocate
     * connections into pools, or some might allocate connections only when
     * requested.
     */
    protected static DbConnectionFactory dbConnectionFactory;

    /**
     * Private, empty constructor so that this class cannot be instantiated.
     */
    private Vault() {
    }

    /**
     * Set the delegate class for managing JDBC database connection pools. This
     * interface allows us to deploy <code>Vault</code> in different
     * environments and using different database connection pooling models.
     */
    public static void setDbConnectionFactory(DbConnectionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        dbConnectionFactory = factory;
    }

    /**
     * Retrieve a database connection from the database connection factory.
     * Depending on the implementation of the delegate
     * {@link DbConnectionFactory DbConnectionFactory}, this method may wait
     * until a connection is available.
     * 
     * @return A database connection.
     * @throws SQLException
     *             If a SQL error occurs while requesting or allocating the
     *             connection. This depends on the implementation of
     *             {@link DbConnectionFactory DbConnectionFactory}being used.
     * @throws IllegalStateException
     *             If no {@link DbConnectionFactory DbConnectionFactory} has
     *             been specified.
     */
    public static Connection getDbConnection() throws SQLException {
        if (dbConnectionFactory == null) {
            throw new IllegalStateException("You must set a DbConnectionFactory before requesting a database connection.");
        }

        return (dbConnectionFactory.getConnection());
    }

    /**
     * Replace a database connection from the database connection factory.
     * 
     * @param connection
     *            the connection to release
     * @throws SQLException
     *             If a SQL error occurs while replacing or deallocating the
     *             connection. This depends on the implementation of
     *             {@link DbConnectionFactory DbConnectionFactory}being used.
     * @throws IllegalStateException
     *             If no {@link DbConnectionFactory DbConnectionFactory} has
     *             been specified.
     */
    public static void releaseDbConnection(Connection connection) throws SQLException {
        if (dbConnectionFactory == null) {
            throw new IllegalStateException("You must set a DbConnectionFactory before releasing a database connection.");
        }

        dbConnectionFactory.releaseConnection(connection);
    }

    /**
     * Set the application configuration properties.
     */
    public static void setProperties(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Vault.properties = properties;

        // for backwards compatibility; put all these
        // properties into the system properties
        java.util.Enumeration keys = properties.keys();
        Properties sysProps = System.getProperties();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            sysProps.put(key, properties.get(key));
        }
    }

    /**
     * Return the entire set of application configuration properties.
     */
    public static Properties getProperties() {
        return (properties);
    }

    /**
     * Return property from the configuration parameter list.
     */
    public static String getProperty(String key) {
        return (properties.getProperty(key));
    }

    /**
     * Set the directory so we will know where we can get the OpenNMS
     * configuration files.
     */
    public static void setHomeDir(String homeDir) {
        if (homeDir == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Vault.homeDir = homeDir;
        Vault.properties.setProperty("opennms.home", homeDir);
        System.setProperty("opennms.home", homeDir);
    }

    /**
     * Get the directory that holds the OpenNMS configuration files.
     */
    public static String getHomeDir() {
        return (homeDir);
    }

    /**
     * <P>
     * Adds new keys to the system properties using the passed key name a the
     * properties location instance. The passed key is used as a key to the
     * system {@link java.util.Properties#getPropertyget property} to find the
     * supplementary property information. The returned value should be in the
     * form of a list of file names, each separated by the system
     * {@link java.io.File#pathSeparatorCharpath separator} character.
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
     * 
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
                        } catch (IOException ioE) {
                            throw ioE;
                        } finally {
                            try {
                                is.close();
                            } catch (IOException ioE) { /* do nothing */
                            }
                        }

                        Iterator i = p.entrySet().iterator();
                        while (i.hasNext()) {
                            Map.Entry e = (Map.Entry) i.next();
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

} // end Vault class.

