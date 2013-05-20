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

package org.opennms.netmgt.provision.detector.generic;

import org.opennms.netmgt.provision.detector.generic.client.GpClient;
import org.opennms.netmgt.provision.detector.generic.request.GpRequest;
import org.opennms.netmgt.provision.detector.generic.response.GpResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>GpDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class GpDetector extends BasicDetector<GpRequest, GpResponse>{
    
    private static String DEFAULT_HOPTION = "--hostname";
    private static String DEFAULT_TOPTION = "--timeout";
    
    private String m_script;
    private String m_args;
    private String m_banner;
    private String m_match;
    private String m_hoption = DEFAULT_HOPTION;
    private String m_toption = DEFAULT_TOPTION;
    
    /**
     * <p>Constructor for GpDetector.</p>
     */
    protected GpDetector() {
        super("GP", 0);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<GpRequest, GpResponse> getClient() {
        final GpClient client = new GpClient();
        client.setScript(getScript());
        client.setArgs(getArgs());
        client.setBanner(getBanner());
        client.setMatch(getMatch());
        client.setHoption(getHoption());
        client.setToption(getToption());
        return client;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(responseMatches(getBanner()));
    }

    private static ResponseValidator<GpResponse> responseMatches(final String banner) {
        return new ResponseValidator<GpResponse>(){

            @Override
            public boolean validate(final GpResponse response) {
                return response.validate(banner);
            }
            
        };
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

}
