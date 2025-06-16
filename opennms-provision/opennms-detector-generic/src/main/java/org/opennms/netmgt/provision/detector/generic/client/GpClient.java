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
package org.opennms.netmgt.provision.detector.generic.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.core.utils.ExecRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.ScriptUtil;
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
        
        ExecRunner execRunner = new ExecRunner();
        execRunner.setMaxRunTimeSecs(convertToSeconds(timeout));
        final String hostAddress = InetAddressUtils.str(address);

        if (!ScriptUtil.isDescendantOf(System.getProperty("opennms.home"), getScript())) {
            throw new IOException("The location of the script must not be outside $OPENNMS_HOME.");
        }

		final String script = "" + getScript() + " " + getHoption() + " " + hostAddress + " " + getToption() + " " + convertToSeconds(timeout);
        if (getArgs() == null)
            setExitStatus(execRunner.exec(script));
        else
            setExitStatus(execRunner.exec(getScript() + " " + getHoption() + " " + hostAddress + " " + getToption() + " " + convertToSeconds(timeout) + " " + getArgs()));
        
        if (execRunner.isMaxRunTimeExceeded()) {
            
        } else {
            if (getExitStatus() == 0) {
                setResponse(execRunner.getOutString());
                setError(execRunner.getErrString());
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
