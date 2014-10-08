/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
