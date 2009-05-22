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
package org.opennms.netmgt.provision.detector.generic;

import org.opennms.netmgt.provision.detector.generic.client.GpClient;
import org.opennms.netmgt.provision.detector.generic.request.GpRequest;
import org.opennms.netmgt.provision.detector.generic.response.GpResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
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
    
    protected GpDetector() {
        super("GP", 0);
    }

    @Override
    protected Client<GpRequest, GpResponse> getClient() {
        GpClient client = new GpClient();
        client.setScript(getScript());
        client.setArgs(getArgs());
        client.setBanner(getBanner());
        client.setMatch(getMatch());
        client.setHoption(getHoption());
        client.setToption(getToption());
        return client;
    }

    @Override
    protected void onInit() {
        expectBanner(responseMatches(getBanner()));
    }

    private ResponseValidator<GpResponse> responseMatches(final String banner) {
        return new ResponseValidator<GpResponse>(){

            public boolean validate(GpResponse response) throws Exception {
                
                return response.validate(banner);
            }
            
        };
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

}
