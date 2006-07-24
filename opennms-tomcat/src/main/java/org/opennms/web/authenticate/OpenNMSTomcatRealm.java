//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2004 Eric Molitor (eric@tuxbot.com)
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

package org.opennms.web.authenticate;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.realm.Constants;
import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.util.StringManager;
//import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.users.User;


/**
 * Implements the interface to allow Tomcat to check our users.xml file
 * to authenticate users.
 * <p/>
 * <p>This class is Tomcat-specific and will not be portable to other
 * servlet containers. It relies on packages supplied with Tomcat.</p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="mailto:eric@tuxbot.com">Eric Molitor</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class OpenNMSTomcatRealm extends RealmBase {
    /**
     * The relative path to find the users.xml file
     */
    protected String HOME_DIR = "/opt/OpenNMS/";

    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String m_info = "org.opennms.web.authenticate.OpenNMSTomcatRealm/1.0";

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String m_name = "OpenNMSTomcatRealm";

        /**
     * The global JNDI name of the <code>UserDatabase</code> resource
     * we will be utilizing.
     */
    protected String m_resourceName = "UserDatabase";

    /**
     * The string manager for this package.
     */
//    private static StringManager sm =
//        StringManager.getManager(Constants.Package);

    /**
     * The set of valid Principals for this Realm, keyed by user name.
     */
    protected HashMap m_principals = new HashMap();

    /**
     * Convenient support for <em>PropertyChangeEvents</em>.
     */
    protected PropertyChangeSupport m_propertyChangeSupport;

    /**
     * The magic-users.properties file that is read for the list of special
     * users, their passwords, and authorization roles.
     */
    protected File m_magicUsersFile;

    /**
     * The time (in milliseconds) that the magic-users.properties file was
     * last modified.  This value is kept so that the users.xml
     * file will be reparsed anytime it is modified.
     */
    protected long m_magicUsersLastModified = 0;

    /**
     * The Log4J category for logging web authentication messages.
     */
    //protected Category log = Authentication.log;

    /**
     * A mapping of special roles to authorized users.  Each role name key
     * contains a <code>List</code> value of authorized user names.
     */
    protected Map m_magicRoleMapping = new HashMap();
    
    /**
     * Indicates that the user factory has been initialized
     */
    protected boolean m_initialized = false;

    /**
     * Create a new instance.
     */
    public OpenNMSTomcatRealm() {
        containerLog = new OurLogger();
        m_propertyChangeSupport = new PropertyChangeSupport(this);
        Vault.getProperties().setProperty("opennms.home", HOME_DIR);
    }

    /**
     * Return the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     */
    public String getResourceName() {
        return m_resourceName;
    }


    /**
     * Set the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     *
     * @param resourceName The new global JNDI name
     */
    public void setResourceName(String resourceName) {

        m_resourceName = resourceName;

    }

    /**
     * Convenience method for parsing the users.xml file.
     * <p/>
     * <p>This method is synchronized so only one thread at a time
     * can parse the users.xml file and create the <code>principal</code>
     * instance variable.</p>
     */
    protected synchronized void parse() {
        //reset the principals cache        
        m_principals = new HashMap();

        try {
            UserFactory.init();
            UserManager factory = UserFactory.getInstance();
            containerLog.debug("Reloaded the users.xml file into memory");

            Map map = factory.getUsers();
            containerLog.debug("Loaded " + map.size() + " users into memory");

            Iterator iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                OpenNMSPrincipal principal = new OpenNMSPrincipal((User) map.get(key));
                m_principals.put(key, principal);
            }

            containerLog.debug("Loaded the regular users into the principal cache");
        } catch (MarshalException e) {
            containerLog.error("Could not parse the users.xml file", e);
        } catch (ValidationException e) {
            containerLog.error("Could not parse the users.xml file", e);
        } catch (FileNotFoundException e) {
            containerLog.error("Could not find the users.xml file", e);
        } catch (Exception e) {
            containerLog.error("Unexpected exception parsing users.xml file", e);
        }

        try {
            //load the "magic" users
            Map[] maps = parseMagicUsers();
            Map magicUserToPasswordMapping = maps[0];
            m_magicRoleMapping = maps[1];
            containerLog.debug("Loaded the magic user config file");

            Iterator iterator = magicUserToPasswordMapping.keySet().iterator();

            while (iterator.hasNext()) {
                String name = (String) iterator.next();
                String password = (String) magicUserToPasswordMapping.get(name);

                User magicUser = new User();
                magicUser.setUserId(name);
                magicUser.setPassword(UserFactory.getInstance().encryptedPassword(password));

                m_principals.put(name, new OpenNMSPrincipal(magicUser));
            }

            containerLog.debug("Loaded the magic users into the principal cache");

            m_magicUsersLastModified = m_magicUsersFile.lastModified();
            containerLog.debug("Updated the magic user file last modified time stamp to " + m_magicUsersLastModified);
        } catch (FileNotFoundException e) {
            containerLog.error("Could not find the magic users file", e);
        } catch (IOException e) {
            containerLog.error("Could not read the magic users file", e);
        } catch (Exception e) {
            containerLog.error("Unexpected exception parsing users.xml file", e);
        }
        
        initialized = true;
    }

    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {
        return container;
    }

    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {
        this.container = container;
        containerLog.info("Initialized with container: "
                           + this.container.getName()
                           + " (" + this.container.getInfo() + ")");
    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (m_info);
    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username    Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *                    authenticating this username
     */
    public synchronized Principal authenticate(String username, String credentials) {
        if (username == null || credentials == null) {
            //throw new IllegalArgumentException( "Cannot take null parameters." );
            return null;
        }

        //check everytime to see if the users.xml file has changed
        if (isParseNecessary()) {
            parse();
        }

        OpenNMSPrincipal principal = (OpenNMSPrincipal) m_principals.get(username);

        if (principal != null && !principal.comparePasswords(credentials)) {
            principal = null;

            containerLog.info("Wrong password for " + username);
        }

        if (principal == null) {
            containerLog.info("Could not authenticate " + username);
        } else {
            containerLog.info("Authenticated " + username);
        }

        return (principal);
    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username    Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *                    authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {
        return (authenticate(username, credentials.toString()));
    }


    /**
     * Not implemented.
     *
     * @throws IllegalStateException because this method is not implemented.
     */
    public Principal authenticate(String username, String digest,
                                  String nonce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {
        throw new IllegalStateException("Not implementing this method for now.");
    }


    /**
     * Not implemented.
     *
     * @throws IllegalStateException because this method is not implemented.
     */
    public Principal authenticate(java.security.cert.X509Certificate[] certs) {
        throw new IllegalStateException("Not implementing this method for now.");
    }

    /**
     * Returns true for any specified user if the role is
     * {@link Authentication#USER_ROLE Authentication.USER_ROLE},
     * and will additionally return true for the <em>admin</em> user if
     * the role is
     * {@link Authentication#ADMIN_ROLE Authentication.ADMIN_ROLE}.
     * Otherwise this method returns false.
     * <p/>
     * <p>Note that no logging takes place in this method because
     * it is called very frequently.  Logging messages here could greatly
     * reduce page-serving performance and would quickly flood the server
     * logs with not very useful information.</p>
     *
     * @param principal
     * @param role      role to be checked
     */
    public boolean hasRole(Principal principal, String role) {
        boolean hasrole = false;

        if (Authentication.USER_ROLE.equals(role)) {
            hasrole = true;            
        } else {
            List userList = (List) m_magicRoleMapping.get(role);

            if (userList != null && userList.contains(principal.getName())) {
                hasrole = true;
            }
        }

        return (hasrole);
    }

    /**
     * Add a property change listener to this component.
     * 
     * @param listener
     *            The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener from this component.
     * 
     * @param listener
     *            The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        m_propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected String getName() {
        return m_name;
    }

    protected String getPassword(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Principal getPrincipal(String userName) {
        return (OpenNMSPrincipal) m_principals.get(userName);
    }


    /**
     * Called by tomcat to handle the <em>userFile</em> attribute in the
     * <em>Realm</em> tag in the server.xml file.
     * 
     * @deprecated
     */
    public void setUserFile(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        containerLog.warn("userFile attribute used, but is deprecated.  Please use homeDir attribute instead.");
    }

    /**
     * Called by tomcat to set the home directory where the app is running from
     * and to handle the <em>userFile</em> attribute in the <em>Realm</em>
     * tag in the server.xml file.
     */
    public void setHomeDir(String homeDir) {
        if (homeDir == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Vault.setHomeDir(homeDir);

        // configure the files to the given home dir
        m_magicUsersFile = new File(homeDir + File.separator + "etc" + File.separator + ConfigFileConstants.getFileName(ConfigFileConstants.MAGIC_USERS_CONF_FILE_NAME));

        containerLog.debug("HomeDir=" + homeDir);
        containerLog.debug("MagicUsersFile=" + m_magicUsersFile);
    }

    /**
     * Checks the last modified time of the user and magic users files against
     * the last known last modified time. If the times are different, then the
     * files must be reparsed.
     * 
     * <p>
     * Note that the <code>lastModified</code> variables are not set here.
     * This is in case there is a problem parsing either file. If we set the
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again. Instead, when we see the file
     * changes, we continue parsing attempts until the parsing succeeds.
     * </p>
     */
    protected boolean isParseNecessary() {
        boolean necessary = false;
        
        if (!initialized) {
            return true;
	}

        if (UserFactory.getInstance().isUpdateNeeded()) {
            necessary = true;
        }

        if (m_magicUsersFile != null
            && m_magicUsersFile.lastModified() != m_magicUsersLastModified) {
            necessary = true;
        }

        return necessary;
    }

    /**
     * Parses the magic-users.properties file into two mappings: from magic
     * username to password, and from magic role to authorized users of that
     * role.
     */
    protected Map[] parseMagicUsers() throws FileNotFoundException, IOException {
        Map passwordMap = new HashMap();
        Map roleMap = new HashMap();

        // read the file
        Properties props = new Properties();
        props.load(new FileInputStream(m_magicUsersFile));

        // look up users and their passwords
        String[] users = BundleLists.parseBundleList(props.getProperty("users"));

        for (int i = 0; i < users.length; i++) {
            String username = props.getProperty("user." + users[i] + ".username");
            String password = props.getProperty("user." + users[i] + ".password");

            passwordMap.put(username, password);
        }

        // look up roles and their users
        String[] roles = BundleLists.parseBundleList(props.getProperty("roles"));

        for (int i = 0; i < roles.length; i++) {
            String rolename = props.getProperty("role." + roles[i] + ".name");
            String[] authUsers = BundleLists.parseBundleList(props.getProperty("role." + roles[i] + ".users"));

            roleMap.put(rolename, Arrays.asList(authUsers));
        }

        return (new Map[] { passwordMap, roleMap });
    }

    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @throws org.apache.catalina.LifecycleException
     *          if this component detects a fatal error
     *          that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        try {
            //StandardServer server = (StandardServer) ServerFactory.getServer();
            //Context context = server.getGlobalNamingContext();
            parse();
            //database = (UserDatabase) context.lookup(m_resourceName);
        } catch (Throwable e) {
            containerLog.info("error parsing configuration file", e);
            m_principals = null;
        }
        if (m_principals == null) {
            throw new LifecycleException("no principals found");
        }

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @throws LifecycleException if this component detects a fatal error
     *                            that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // Release reference to our user database
        m_principals = null;

    }

    public class OurLogger implements org.apache.commons.logging.Log {
        public void debug(Object message) {
            log(message);
        }

        public void debug(Object message, Throwable t) {
            log(message, t);
        }

        public void error(Object message) {
            log(message);
        }

        public void error(Object message, Throwable t) {
            log(message, t);
        }

        public void fatal(Object message) {
            log(message);
        }

        public void fatal(Object message, Throwable t) {
            log(message, t);
        }

        public void info(Object message) {
            log(message);
        }

        public void info(Object message, Throwable t) {
            log(message, t);
        }

        public void trace(Object message) {
            log(message);
        }

        public void trace(Object message, Throwable t) {
            log(message, t);
        }

        public void warn(Object message) {
            log(message);
        }

        public void warn(Object message, Throwable t) {
            log(message, t);
        }

        public boolean isDebugEnabled() {
            return true;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        public boolean isFatalEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

        public boolean isTraceEnabled() {
            return true;
        }

        public boolean isWarnEnabled() {
            return true;
        }

        private void log(Object message) {
            log(message, null);
        }

        private void log(Object message, Throwable e) {
            System.err.println(message);
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
}
