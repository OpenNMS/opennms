/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

/**
 * @author Donald Desloge
 *
 */
public class Jsr160Detector extends JMXDetector{

    private static int DEFAULT_PORT = 9003;
    private static int DEFAULT_TIMEOUT = 1000;
    private static int DEFAULT_RETRIES = 0;
    
    private String m_factory = "STANDARD";
    private String m_friendlyName = "jsr160";
    private String m_protocol = "rmi";
    private String m_type = "default";
    private String m_urlPath = "/jmxrmi";
    private String m_username = "opennms";
    private String m_password = "OPENNMS";
    
    protected Jsr160Detector() {
        super(DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
        setServiceName("JSR160");

    }

    @Override
    protected JMXClient getClient() {
        Jsr160Client client = new Jsr160Client();
        client.setFactory(getFactory());
        client.setFriendlyName(getFriendlyName());
        client.setProtocol(getProtocol());
        client.setUrlPath(getUrlPath());
        client.setUsername(getUsername());
        client.setPassword(getPassword());
        client.setType(getType());
        
        return new Jsr160Client();
    }

    @Override
    protected void onInit() {
        expectBeanCount(greatThan(0));
    }

    public void setFactory(String factory) {
        m_factory = factory;
    }

    public String getFactory() {
        return m_factory;
    }

    public void setFriendlyName(String friendlyName) {
        m_friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return m_friendlyName;
    }

    public void setProtocol(String protocol) {
        m_protocol = protocol;
    }

    public String getProtocol() {
        return m_protocol;
    }

    public void setType(String type) {
        m_type = type;
    }

    public String getType() {
        return m_type;
    }

    public void setUrlPath(String urlPath) {
        m_urlPath = urlPath;
    }

    public String getUrlPath() {
        return m_urlPath;
    }

    public void setUsername(String username) {
        m_username = username;
    }

    public String getUsername() {
        return m_username;
    }

    public void setPassword(String password) {
        m_password = password;
    }

    public String getPassword() {
        return m_password;
    }

}
