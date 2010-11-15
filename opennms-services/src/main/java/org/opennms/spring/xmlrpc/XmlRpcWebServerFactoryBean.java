/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 13, 2005
 * 
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.spring.xmlrpc;

import java.net.InetAddress;

import org.apache.xmlrpc.WebServer;
import org.apache.xmlrpc.XmlRpcServer;
import org.apache.xmlrpc.secure.SecureWebServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>XmlRpcWebServerFactoryBean class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class XmlRpcWebServerFactoryBean implements FactoryBean<WebServer>, InitializingBean, DisposableBean {
    
    WebServer webServer;
    int port = -1;
    InetAddress address = null;
    XmlRpcServer xmlRpcServer = null;
    boolean secure = false;

    /**
     * <p>Getter for the field <code>port</code>.</p>
     *
     * @return a int.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * <p>Setter for the field <code>port</code>.</p>
     *
     * @param port a int.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * <p>Getter for the field <code>address</code>.</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return this.address;
    }
    
    /**
     * <p>Setter for the field <code>address</code>.</p>
     *
     * @param addrress a {@link java.net.InetAddress} object.
     */
    public void setAddress(InetAddress addrress) {
        this.address = addrress;
    }
    
    /**
     * <p>Getter for the field <code>secure</code>.</p>
     *
     * @return a boolean.
     */
    public boolean getSecure() {
        return this.secure;
    }

    /**
     * <p>Setter for the field <code>secure</code>.</p>
     *
     * @param secure a boolean.
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * <p>Getter for the field <code>xmlRpcServer</code>.</p>
     *
     * @return a {@link org.apache.xmlrpc.XmlRpcServer} object.
     */
    public XmlRpcServer getXmlRpcServer() {
        return this.xmlRpcServer;
    }

    /**
     * <p>Setter for the field <code>xmlRpcServer</code>.</p>
     *
     * @param xmlRpcServer a {@link org.apache.xmlrpc.XmlRpcServer} object.
     */
    public void setXmlRpcServer(XmlRpcServer xmlRpcServer) {
        this.xmlRpcServer = xmlRpcServer;
    }

    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public WebServer getObject() throws Exception {
        return webServer;
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<? extends WebServer> getObjectType() {
        return WebServer.class;
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        if (this.port == -1)
            throw new IllegalArgumentException("port is required");
        
        if (this.xmlRpcServer == null)
            this.xmlRpcServer = new XmlRpcServer();
        
        if (secure) {
            webServer = new SecureWebServer(this.port, this.address, this.xmlRpcServer);
        }
        else
            webServer = new WebServer(this.port, this.address, this.xmlRpcServer);
        webServer.start();
    }

    /**
     * <p>destroy</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void destroy() throws Exception {
        webServer.shutdown();
    }
    


}
