/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
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
package org.opennms.netmgt.provision.detector.ssh;

import org.opennms.netmgt.provision.detector.ssh.client.SshClient;
import org.opennms.netmgt.provision.detector.ssh.request.NullRequest;
import org.opennms.netmgt.provision.detector.ssh.response.SshResponse;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.ssh.Ssh;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SshDetector extends BasicDetector<NullRequest, SshResponse>{
    
    private String m_banner = null;
    private String m_match = null;
    private String m_clientBanner = Ssh.DEFAULT_CLIENT_BANNER;
    
    public SshDetector() {
        super(22, 3000, 0);
        setServiceName("SSH");
    }

    @Override
    protected Client<NullRequest, SshResponse> getClient() {
        SshClient client = new SshClient();
        client.setBanner(getBanner());
        client.setMatch(getMatch());
        client.setClientBanner(getClientBanner());
        return client;
    }

    @Override
    protected void onInit() {
        expectBanner(sshIsAvailable());
    }

    /**
     * @return
     */
    private ResponseValidator<SshResponse> sshIsAvailable() {
        
        return new ResponseValidator<SshResponse>(){

            public boolean validate(SshResponse response) throws Exception {
                return response.isAvailable();
            }
            
        };
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

    public void setClientBanner(String clientBanner) {
        m_clientBanner = clientBanner;
    }

    public String getClientBanner() {
        return m_clientBanner;
    }
	
}