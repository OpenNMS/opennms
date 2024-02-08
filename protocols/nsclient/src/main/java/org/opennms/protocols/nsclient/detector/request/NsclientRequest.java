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
package org.opennms.protocols.nsclient.detector.request;

import org.opennms.protocols.nsclient.NsclientCheckParams;
import org.opennms.protocols.nsclient.NsclientManager;

/**
 * <p>NsclientRequest class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientRequest {

    private String command;

    private String parameter;

    private int warnPerc;

    private int critPerc;

    private int retries;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public int getWarnPerc() {
        return warnPerc;
    }

    public void setWarnPerc(int warnPerc) {
        this.warnPerc = warnPerc;
    }

    public int getCritPerc() {
        return critPerc;
    }

    public void setCritPerc(int critPerc) {
        this.critPerc = critPerc;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public NsclientCheckParams getCheckParams() {
        return new NsclientCheckParams(getCritPerc(), getWarnPerc(), getParameter());
    }

    public String getFormattedCommand() {
        return NsclientManager.convertStringToType(getCommand());
    }

    @Override
    public String toString() {
        return "NsclientRequest[command=" + getCommand() + ", parameter=" + getParameter() + "]";
    }

}
