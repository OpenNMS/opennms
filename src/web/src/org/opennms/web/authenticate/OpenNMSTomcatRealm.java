//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
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
import org.apache.catalina.Realm;
import org.apache.log4j.Category;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.users.*;
import org.opennms.netmgt.ConfigFileConstants;


/**
 * Implements the interface to allow Tomcat to check our users.xml file
 * to authenticate users.
 *
 * <p>This class is Tomcat-specific and will not be portable to other 
 * servlet containers. It relies on packages supplied with Tomcat.</p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class OpenNMSTomcatRealm extends Object implements Realm
{
    /**
     * The relative path to find the users.xml file
     */
    protected String HOME_DIR = "/opt/OpenNMS/";
    
    /**
     * The Container with which this Realm is associated.
     */
    protected Container container = null;

    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info = "org.opennms.web.authenticate.OpenNMSTomcatRealm/1.0";

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "OpenNMSTomcatRealm";

    /**
     * The set of valid Principals for this Realm, keyed by user name.
     */
    protected HashMap principals = new HashMap();

    /**
     * Convenient support for <em>PropertyChangeEvents</em>.
     */
    protected PropertyChangeSupport propertyChangeSupport;

    /**
     * The users.xml file that is read for the list of valid
     * users and their passwords.
     */
    protected File xmlFile;
    
    /**
     * The magic-users.properties file that is read for the list of special
     * users, their passwords, and authorization roles.
     */
    protected File magicUsersFile;
    
    /**
     * The time (in milliseconds) that the users.xml file was 
     * last modified.  This value is kept so that the users.xml
     * file will be reparsed anytime it is modified.
     */
    protected long xmlFileLastModified = 0;

    /**
     * The time (in milliseconds) that the magic-users.properties file was 
     * last modified.  This value is kept so that the users.xml
     * file will be reparsed anytime it is modified.
     */
    protected long magicUsersLastModified = 0;
    
    /**
     * The Log4J category for logging web authentication messages.
     */
    protected Category log = Authentication.log;

    /**
     * A mapping of special roles to authorized users.  Each role name key 
     * contains a <code>List</code> value of authorized user names.
     */
    protected Map magicRoleMapping = new HashMap();

    
    /**
     * Create a new instance.
     */
    public OpenNMSTomcatRealm() {
        this.propertyChangeSupport = new PropertyChangeSupport( this );
        Vault.getProperties().setProperty("opennms.home", HOME_DIR);


    }


    /** 
     * Convenience method for parsing the users.xml file.
     *
     * <p>This method is synchronized so only one thread at a time 
     * can parse the users.xml file and create the <code>principal</code>
     * instance variable.</p>
     */
    protected synchronized void parse() {
        //reset the principals cache        
        this.principals = new HashMap();
        
        try {
            //load the regular users            
            UserFactory.reload();
            UserFactory factory = UserFactory.getInstance();
            this.log.debug( "Reloaded the users.xml file into memory" );
        
            Map map = factory.getUsers();
            this.log.debug( "Loaded " + map.size() + " users into memory" );

            Iterator iterator = map.keySet().iterator();
    
            while( iterator.hasNext() ) {
                String key = (String)iterator.next();
                OpenNMSPrincipal principal = new OpenNMSPrincipal( (User)map.get( key ));
                this.principals.put( key, principal );
            }
            
            this.log.debug( "Loaded the regular users into the principal cache" );

            //this.xmlFileLastModified = this.xmlFile.lastModified();
            this.log.debug( "Updated the users.xml file last modified time stamp to " + this.xmlFileLastModified );            
        }
        catch( MarshalException e ) {
            this.log.error( "Could not parse the users.xml file", e );
        }
        catch( ValidationException e ) {
            this.log.error( "Could not parse the users.xml file", e );
        }
        catch( FileNotFoundException e ) {
            this.log.error( "Could not find the users.xml file", e );
        }
        catch( Exception e ) {
            this.log.error( "Unexpected exception parsing users.xml file", e );
        }            

        try {
            //load the "magic" users
            Map[] maps = this.parseMagicUsers();                        
            Map magicUserToPasswordMapping = maps[0];
            this.magicRoleMapping = maps[1];
            this.log.debug( "Loaded the magic user config file" );
                        
            Iterator iterator = magicUserToPasswordMapping.keySet().iterator();                        

            while( iterator.hasNext() ) {
                String name = (String)iterator.next();
                String password = (String)magicUserToPasswordMapping.get(name);
                
                User magicUser = new User();
                magicUser.setUserId(name);
                magicUser.setPassword(UserFactory.encryptPassword(password));

                this.principals.put(name, new OpenNMSPrincipal(magicUser));                
            }
                                                            
            this.log.debug( "Loaded the magic users into the principal cache" );    
            
            this.magicUsersLastModified = this.magicUsersFile.lastModified();
            this.log.debug( "Updated the magic user file last modified time stamp to " + this.magicUsersLastModified );
        }
        catch( FileNotFoundException e ) {
            this.log.error( "Could not find the magic users file", e );
        }
        catch( IOException e ) {
            this.log.error( "Could not read the magic users file", e );
        }        
        catch( Exception e ) {
            this.log.error( "Unexpected exception parsing users.xml file", e );
        }
    }


    /**
     * Return the Container with which this Realm has been associated.
     */
    public Container getContainer() {
        return( this.container );
    }


    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {
        this.container = container;

        this.log.debug( "Initialized with container: " + this.container.getName() + " (" + this.container.getInfo() + ")" );
    }


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return( this.info );
    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {
        if( username == null || credentials == null ) {
            //throw new IllegalArgumentException( "Cannot take null parameters." );
            return null;
        }

        //check everytime to see if the users.xml file has changed
        //if( this.isParseNecessary() ) {
            this.parse();
        //}

        OpenNMSPrincipal principal = (OpenNMSPrincipal)this.principals.get( username );

        if( principal != null && !principal.comparePasswords( credentials ) ) {
            principal = null;

            this.log.info( "Wrong password for " + username );
        }

        if( principal == null ) {
            this.log.info( "Could not authenticate " + username );
        }
        else {
            this.log.info( "Authenticated " + username );
        }
    
        return( principal );
    }


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, byte[] credentials) {
        return (authenticate(username, credentials.toString()));
    }


    /**
     * Not implemented.
     * @throws IllegalStateException because this method is not implemented.
     */
    public Principal authenticate(String username, String digest,
                                  String nonce, String nc, String cnonce,
                                  String qop, String realm,
                                  String md5a2) {
        throw new IllegalStateException( "Not implementing this method for now." );
    }

    
    /**
     * Not implemented.
     * @throws IllegalStateException because this method is not implemented.
     */
    public Principal authenticate(java.security.cert.X509Certificate[] certs) {
        throw new IllegalStateException( "Not implementing this method for now." );
    }
    

    /**
     * Returns true for any specified user if the role is 
     * {@link Authentication#USER_ROLE Authentication.USER_ROLE},
     * and will additionally return true for the <em>admin</em> user if 
     * the role is
     * {@link Authentication#ADMIN_ROLE Authentication.ADMIN_ROLE}.
     * Otherwise this method returns false.
     *
     * <p>Note that no logging takes place in this method because
     * it is called very frequently.  Logging messages here could greatly
     * reduce page-serving performance and would quickly flood the server 
     * logs with not very useful information.</p>
     *
     * @param principal 
     * @param role role to be checked
     */
    public boolean hasRole(Principal principal, String role) {
        boolean hasrole = false;

        if( Authentication.USER_ROLE.equals(role)) {
            hasrole = true;
        }
        else {
            List userList = (List)this.magicRoleMapping.get(role);
            
            if( userList != null && userList.contains(principal.getName()) ) {
                hasrole = true;
            }
        }
        
        return( hasrole );
    }


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener( listener );
    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener( listener );
    }


    /**
     * Called by tomcat to handle the <em>userFile</em> attribute in the
     * <em>Realm</em> tag in the server.xml file.
     * @deprecated
     */
    public void setUserFile( String filename ) {
        if( filename == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        //this.xmlFile = new File( Vault.getProperty("opennms.home") + File.separator + filename );
        this.log.debug( "XmlFile=" + this.xmlFile );
        this.log.warn( "usserFile attribute used, but is deprecated.  Please use homeDir attribute instead." );        
    }

    
    /**
     * Called by tomcat to set the home directory where the app is running from and 
     * to handle the <em>userFile</em> attribute in the
     * <em>Realm</em> tag in the server.xml file.
     */
    public void setHomeDir( String homeDir ) {
        if( homeDir == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        Vault.setHomeDir(homeDir);
        
        //configure the files to the given home dir
        //this.xmlFile = new File(homeDir + File.separator + ConfigFileConstants.USERS_CONF_FILE_NAME);
        this.magicUsersFile = new File(homeDir + File.separator
					+ "etc" + File.separator
					+ ConfigFileConstants.getFileName(ConfigFileConstants.MAGIC_USERS_CONF_FILE_NAME));
        
        this.log.debug( "HomeDir=" + homeDir );
        this.log.debug( "XmlFile=" + this.xmlFile );
        this.log.debug( "MagicUsersFile=" + this.magicUsersFile );
    }

    
    /**
     * Checks the last modified time of the user and magic users files against
     * the last known last modified time.  If the times are different, then 
     * the files must be reparsed. 
     *
     * <p>Note that the <code>lastModified</code> variables are not set here. 
     * This is in case there is a problem parsing either file.  If we set the 
     * value here, and then try to parse and fail, then we will not try to parse
     * again until the file changes again.  Instead, when we see the file 
     * changes, we continue parsing attempts until the parsing succeeds.</p>
     */
    protected boolean isParseNecessary() {
        boolean necessary = false;

        if( this.xmlFile != null && this.xmlFile.lastModified() != this.xmlFileLastModified ) {
            necessary = true;            
        }
        
        if( this.magicUsersFile != null && this.magicUsersFile.lastModified() != this.magicUsersLastModified ) {
            necessary = true;
        }
                
        return( necessary );
    }


    /**
     * Parses the magic-users.properties file into two mappings: from magic
     * username to password, and from magic role to authorized users of that
     * role.
     */
    protected Map[] parseMagicUsers() throws FileNotFoundException, IOException {
        Map passwordMap = new HashMap();
        Map roleMap = new HashMap();

        //read the file
        Properties props = new Properties();
        props.load(new FileInputStream(this.magicUsersFile));
        
        //look up users and their passwords
        String[] users = BundleLists.parseBundleList(props.getProperty("users"));
        
        for( int i=0; i < users.length; i++ ) {
            String username = props.getProperty("user." + users[i] + ".username");
            String password = props.getProperty("user." + users[i] + ".password");
            
            passwordMap.put(username, password);
        }

        //look up roles and their users
        String[] roles = BundleLists.parseBundleList(props.getProperty("roles"));
        
        for( int i=0; i < roles.length; i++ ) {
            String rolename = props.getProperty("role." + roles[i] + ".name");
            String[] authUsers = BundleLists.parseBundleList(props.getProperty("role." + roles[i] + ".users"));
            
            roleMap.put(rolename, Arrays.asList(authUsers));
        }        
        
        return(new Map[] { passwordMap, roleMap });        
    }
}



