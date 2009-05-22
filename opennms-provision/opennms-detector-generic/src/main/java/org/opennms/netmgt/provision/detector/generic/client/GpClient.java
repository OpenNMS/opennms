/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.provision.detector.generic.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.provision.detector.generic.request.GpRequest;
import org.opennms.netmgt.provision.detector.generic.response.GpResponse;
import org.opennms.netmgt.provision.detector.generic.support.ExecRunner;
import org.opennms.netmgt.provision.support.Client;

public class GpClient implements Client<GpRequest, GpResponse> {
    
    private String m_script;
    private String m_args;
    private String m_banner;
    private String m_match;
    private String m_hoption;
    private String m_toption;
    private ExecRunner m_execRunner;
    private int m_exitStatus = 100;
    private String m_response = "";
    private String m_error = "";
    
    public void close() {
        
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        setExitStatus(100);
        
        m_execRunner = new ExecRunner();
        m_execRunner.setMaxRunTimeSecs(convertToSeconds(timeout));
        String script = "" + getScript() + " " + getHoption() + " " + address.getHostAddress() + " " + getToption() + " " + convertToSeconds(timeout);
        if (getArgs() == null)
            setExitStatus(m_execRunner.exec(script));
        else
            setExitStatus(m_execRunner.exec(getScript() + " " + getHoption() + " " + address.getHostAddress() + " " + getToption() + " " + convertToSeconds(timeout) + " " + getArgs()));
        
        if (m_execRunner.isMaxRunTimeExceeded()) {
            
        } else {
            if (getExitStatus() == 0) {
                setResponse(m_execRunner.getOutString());
                setError(m_execRunner.getErrString());
            }
        }
    }

    private int convertToSeconds(int timeout) {
        if(timeout > 0 && timeout < 1000){
            timeout = 1;
        }else{
            timeout = timeout/1000;
        }
        return timeout;
    }

    public GpResponse receiveBanner() throws IOException, Exception {
        
        return receiveResponse();
    }

    private GpResponse receiveResponse() {
        GpResponse response = new GpResponse();
        response.setExitStatus(getExitStatus());
        response.setResponse(getResponse());
        response.setError(getError());
        return response;
    }

    public GpResponse sendRequest(GpRequest request) throws IOException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void setScript(String script) {
        m_script = script;
    }

    public String getScript() {
        return m_script;
    }

    public void setArgs(String args) {
        m_args = args;
    }

    public String getArgs() {
        return m_args;
    }

    public void setBanner(String banner) {
        m_banner = banner;
    }

    public String getBanner() {
        return m_banner;
    }

    public void setMatch(String match) {
        m_match = match;
    }

    public String getMatch() {
        return m_match;
    }

    public void setHoption(String hoption) {
        m_hoption = hoption;
    }

    public String getHoption() {
        return m_hoption;
    }

    public void setToption(String toption) {
        m_toption = toption;
    }

    public String getToption() {
        return m_toption;
    }

    public void setExitStatus(int exitStatus) {
        m_exitStatus = exitStatus;
    }

    public int getExitStatus() {
        return m_exitStatus;
    }

    public void setResponse(String response) {
        m_response = response;
    }

    public String getResponse() {
        return m_response;
    }

    public void setError(String error) {
        m_error = error;
    }

    public String getError() {
        return m_error;
    }

}
