/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 * *****************************************************************************
 */
package org.opennms.features.jmxconfiggenerator.webui.data;

/**
 * This class encapsulates all parameters for
 * {@link org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator}.
 * Therefore the API of the
 * <code>JmxDatacollectionConfiggenerator</code> can be used and in addition it
 * is possible to provide further information for additional stuff in the
 * future.
 *
 * @author Markus von Rüden
 */
public class ServiceConfig {

    private String serviceName = "anyservice";
    private boolean jmxmp = false;
    private String host = "localhost";
    private String port = "18980";
    private String outFile = "JmxConfig.xml";
    private String user = null;
    private String password = null;
    // FIXME this is never used 
    private boolean ssl = false;
    private boolean skipDefaultVM = false;
    private boolean runWritableMBeans = false;
    private boolean authenticate = false;

    public boolean isAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public boolean isRunWritableMBeans() {
        return runWritableMBeans;
    }

    public void setRunWritableMBeans(boolean runWritableMBeans) {
        this.runWritableMBeans = runWritableMBeans;
    }

    public boolean isSkipDefaultVM() {
        return skipDefaultVM;
    }

    public void setSkipDefaultVM(boolean skipDefaultVM) {
        this.skipDefaultVM = skipDefaultVM;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isJmxmp() {
        return jmxmp;
    }

    public void setJmxmp(boolean jmx) {
        this.jmxmp = jmx;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
