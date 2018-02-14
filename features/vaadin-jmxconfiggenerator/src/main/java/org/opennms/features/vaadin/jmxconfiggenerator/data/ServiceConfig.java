/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.jmxconfiggenerator.data;

/**
 * This class encapsulates all parameters for
 * {@link org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator}.
 * Therefore the API of the <code>JmxDatacollectionConfiggenerator</code> can be used.
 *
 * @author Markus von Rüden
 */
public class ServiceConfig {

    private String serviceName = "anyservice";
    private String outFile = "JmxConfig.xml";
    private String user = null;
    private String password = null;
    private boolean skipDefaultVM = true;
    private boolean skipNonNumber = false;
    private boolean authenticate = false;
    private String connection = "service:jmx:rmi://localhost:18980";

    public boolean isAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public boolean isSkipDefaultVM() {
        return skipDefaultVM;
    }

    public void setSkipDefaultVM(boolean skipDefaultVM) {
        this.skipDefaultVM = skipDefaultVM;
    }

    public boolean isSkipNonNumber() {
        return skipNonNumber;
    }

    public void setSkipNonNumber(boolean skipNonNumber) {
        this.skipNonNumber = skipNonNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }
}
