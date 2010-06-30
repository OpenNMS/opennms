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

/**
 * <p>GpClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    
    /**
     * <p>close</p>
     */
    public void close() {
        
    }

    /** {@inheritDoc} */
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

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.generic.response.GpResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
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

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.detector.generic.request.GpRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.generic.response.GpResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public GpResponse sendRequest(GpRequest request) throws IOException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>setScript</p>
     *
     * @param script a {@link java.lang.String} object.
     */
    public void setScript(String script) {
        m_script = script;
    }

    /**
     * <p>getScript</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getScript() {
        return m_script;
    }

    /**
     * <p>setArgs</p>
     *
     * @param args a {@link java.lang.String} object.
     */
    public void setArgs(String args) {
        m_args = args;
    }

    /**
     * <p>getArgs</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getArgs() {
        return m_args;
    }

    /**
     * <p>setBanner</p>
     *
     * @param banner a {@link java.lang.String} object.
     */
    public void setBanner(String banner) {
        m_banner = banner;
    }

    /**
     * <p>getBanner</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBanner() {
        return m_banner;
    }

    /**
     * <p>setMatch</p>
     *
     * @param match a {@link java.lang.String} object.
     */
    public void setMatch(String match) {
        m_match = match;
    }

    /**
     * <p>getMatch</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMatch() {
        return m_match;
    }

    /**
     * <p>setHoption</p>
     *
     * @param hoption a {@link java.lang.String} object.
     */
    public void setHoption(String hoption) {
        m_hoption = hoption;
    }

    /**
     * <p>getHoption</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHoption() {
        return m_hoption;
    }

    /**
     * <p>setToption</p>
     *
     * @param toption a {@link java.lang.String} object.
     */
    public void setToption(String toption) {
        m_toption = toption;
    }

    /**
     * <p>getToption</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getToption() {
        return m_toption;
    }

    /**
     * <p>setExitStatus</p>
     *
     * @param exitStatus a int.
     */
    public void setExitStatus(int exitStatus) {
        m_exitStatus = exitStatus;
    }

    /**
     * <p>getExitStatus</p>
     *
     * @return a int.
     */
    public int getExitStatus() {
        return m_exitStatus;
    }

    /**
     * <p>setResponse</p>
     *
     * @param response a {@link java.lang.String} object.
     */
    public void setResponse(String response) {
        m_response = response;
    }

    /**
     * <p>getResponse</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResponse() {
        return m_response;
    }

    /**
     * <p>setError</p>
     *
     * @param error a {@link java.lang.String} object.
     */
    public void setError(String error) {
        m_error = error;
    }

    /**
     * <p>getError</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getError() {
        return m_error;
    }

}
