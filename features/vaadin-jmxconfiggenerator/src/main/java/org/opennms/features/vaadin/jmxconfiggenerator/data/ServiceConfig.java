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
