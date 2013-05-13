/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.detector.generic.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.detector.generic.request.GpRequest;
import org.opennms.netmgt.provision.detector.generic.response.GpResponse;
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
    @Override
    public void close() {
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        setExitStatus(100);
        
        m_execRunner = new ExecRunner();
        m_execRunner.setMaxRunTimeSecs(convertToSeconds(timeout));
        final String hostAddress = InetAddressUtils.str(address);
		final String script = "" + getScript() + " " + getHoption() + " " + hostAddress + " " + getToption() + " " + convertToSeconds(timeout);
        if (getArgs() == null)
            setExitStatus(m_execRunner.exec(script));
        else
            setExitStatus(m_execRunner.exec(getScript() + " " + getHoption() + " " + hostAddress + " " + getToption() + " " + convertToSeconds(timeout) + " " + getArgs()));
        
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
    @Override
    public GpResponse receiveBanner() throws IOException, Exception {
        
        return receiveResponse();
    }

    private GpResponse receiveResponse() {
        final GpResponse response = new GpResponse();
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
    @Override
    public GpResponse sendRequest(final GpRequest request) throws IOException, Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * <p>setScript</p>
     *
     * @param script a {@link java.lang.String} object.
     */
    public void setScript(final String script) {
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
    public void setArgs(final String args) {
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
    public void setBanner(final String banner) {
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
    public void setMatch(final String match) {
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
    public void setHoption(final String hoption) {
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
    public void setToption(final String toption) {
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
    public void setExitStatus(final int exitStatus) {
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
    public void setResponse(final String response) {
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
    public void setError(final String error) {
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
