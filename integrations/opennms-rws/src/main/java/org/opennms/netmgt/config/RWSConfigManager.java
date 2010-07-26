//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 May 06: Eliminate a warning. - dj@opennms.org
// 2006 Apr 27: Added support for pathOutageEnabled
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.rws.BaseUrl;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.rws.StandbyUrl;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.rancid.ConnectionProperties;

/**
 * <p>Abstract RWSConfigManager class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
abstract public class RWSConfigManager implements RWSConfig {
    
    private int cursor = 0;
    /**
     * <p>getBase</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    public ConnectionProperties getBase() {
        log().debug("Connections used : " +getBaseUrl().getServer_url()+getBaseUrl().getDirectory());
        log().debug("RWS timeout(sec): "+getBaseUrl().getTimeout());
        if (getBaseUrl().getUsername() == null)
            return new ConnectionProperties(getBaseUrl().getServer_url(),getBaseUrl().getDirectory(),getBaseUrl().getTimeout());
        String password = "";
        if (getBaseUrl().getPassword() != null)
            password = getBaseUrl().getPassword();
        return new ConnectionProperties(getBaseUrl().getUsername(),password,getBaseUrl().getServer_url(),getBaseUrl().getDirectory(),getBaseUrl().getTimeout());
    }

    /**
     * <p>getNextStandBy</p>
     *
     * @return a {@link org.opennms.rancid.ConnectionProperties} object.
     */
    public ConnectionProperties getNextStandBy() {
        if (! hasStandbyUrl()) return null; 
        StandbyUrl standByUrl = getNextStandbyUrl();
        log().debug("Connections used : " +standByUrl.getServer_url()+standByUrl.getDirectory());
        log().debug("RWS timeout(sec): "+standByUrl.getTimeout());
        if (standByUrl.getUsername() == null)
            return new ConnectionProperties(standByUrl.getServer_url(),standByUrl.getDirectory(),standByUrl.getTimeout());
        String password = "";
        if (standByUrl.getPassword() != null)
            password = standByUrl.getPassword();
        return new ConnectionProperties(standByUrl.getUsername(),password,standByUrl.getServer_url(),standByUrl.getDirectory(),standByUrl.getTimeout());
    }

    /**
     * <p>getStandBy</p>
     *
     * @return an array of {@link org.opennms.rancid.ConnectionProperties} objects.
     */
    public ConnectionProperties[] getStandBy() {
        // TODO Auto-generated method stub
        return null;
    }

    
    /**
     * <p>getBaseUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.BaseUrl} object.
     */
    public synchronized BaseUrl getBaseUrl() {
        
        BaseUrl url = m_config.getBaseUrl();
        return url;
    }
 
    /**
     * <p>getStanbyUrls</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.rws.StandbyUrl} objects.
     */
    public synchronized StandbyUrl[] getStanbyUrls() {
        
        return m_config.getStandbyUrl();

    }

    /**
     * <p>getNextStandbyUrl</p>
     *
     * @return a {@link org.opennms.netmgt.config.rws.StandbyUrl} object.
     */
    public synchronized StandbyUrl getNextStandbyUrl() {
        StandbyUrl standbyUrl = null;
        if (hasStandbyUrl()) {
            if (cursor == m_config.getStandbyUrlCount())   
                cursor = 0;
            standbyUrl = m_config.getStandbyUrl(cursor++);
        }
        
        return standbyUrl;
    }
    
    /**
     * <p>hasStandbyUrl</p>
     *
     * @return a boolean.
     */
    public synchronized boolean hasStandbyUrl() {
        return (m_config.getStandbyUrlCount() > 0);
    }

    /**
     * <p>Constructor for RWSConfigManager.</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    public RWSConfigManager(Reader reader) throws MarshalException, ValidationException, IOException {
        reloadXML(reader);
    }

    /**
     * <p>Constructor for RWSConfigManager.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public RWSConfigManager(InputStream stream) throws MarshalException, ValidationException, IOException {
        reloadXML(stream);
    }

    /**
     * <p>Constructor for RWSConfigManager.</p>
     */
    public RWSConfigManager() {
    }
    
    //    public abstract void update() throws IOException, MarshalException, ValidationException;
//
//    protected abstract void saveXml(String xml) throws IOException;
//
//    /**
//     * The config class loaded from the config file
//     */
    private RwsConfiguration m_config;

    /**
     * <p>reloadXML</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(RwsConfiguration.class, reader);
    }

    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected synchronized void reloadXML(InputStream stream) throws MarshalException, ValidationException, IOException {
        m_config = CastorUtils.unmarshal(RwsConfiguration.class, stream);
    }

//    /**
//     * Saves the current in-memory configuration to disk and reloads
//     */
//    public synchronized void save() throws MarshalException, IOException, ValidationException {
//    
//        // marshall to a string first, then write the string to the file. This
//        // way the original config
//        // isn't lost if the xml from the marshall is hosed.
//        StringWriter stringWriter = new StringWriter();
//        Marshaller.marshal(m_config, stringWriter);
//        saveXml(stringWriter.toString());
//    
//        update();
//    }

    /**
     * Return the poller configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.rws.RwsConfiguration} object.
     */
    public synchronized RwsConfiguration getConfiguration() {
        return m_config;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

     
}
