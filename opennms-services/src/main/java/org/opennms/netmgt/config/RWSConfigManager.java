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
import java.io.Reader;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.rws.RwsConfiguration;
import org.opennms.netmgt.config.rws.StandbyUrl;

import org.opennms.netmgt.config.rws.BaseUrl;
import org.opennms.rancid.ConnectionProperties;

/**
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@openms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
abstract public class RWSConfigManager implements RWSConfig {
    
    private int cursor = 0;
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

    public ConnectionProperties[] getStandBy() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public synchronized BaseUrl getBaseUrl() {
        
        BaseUrl url = m_config.getBaseUrl();
        return url;
    }
 
    public synchronized StandbyUrl[] getStanbyUrls() {
        
        return m_config.getStandbyUrl();

    }

    /**
     * 
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
    
     public synchronized boolean hasStandbyUrl() {

         return (m_config.getStandbyUrlCount() > 0);
        
    }

    public RWSConfigManager(Reader reader) throws MarshalException, ValidationException, IOException {
        reloadXML(reader);
    }

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

    protected synchronized void reloadXML(Reader reader) throws MarshalException, ValidationException, IOException {
        m_config = (RwsConfiguration) Unmarshaller.unmarshal(RwsConfiguration.class, reader);
        // call the init methids that populate local object
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
     */
    public synchronized RwsConfiguration getConfiguration() {
        return m_config;
    }

    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

     
}
