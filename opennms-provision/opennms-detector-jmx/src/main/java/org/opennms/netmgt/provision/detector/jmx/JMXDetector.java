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
package org.opennms.netmgt.provision.detector.jmx;

import org.opennms.netmgt.provision.detector.jmx.client.JMXClient;
import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;


public abstract class JMXDetector extends BasicDetector<ConnectionWrapper, Integer>{

    protected JMXDetector(String serviceName, int port) {
        super(serviceName, port);
    }
    
    protected JMXDetector(String serviceName, int port, int timeout, int retries) {
        super(serviceName, port, timeout, retries);
    }

    
    @Override
    protected abstract JMXClient getClient();

    
    @Override
    protected abstract void onInit();
    
    protected void expectBeanCount(ResponseValidator<Integer> bannerValidator) {
        getConversation().expectBanner(bannerValidator);
    }
    
    protected ResponseValidator<Integer> greatThan(final int count){
        return new ResponseValidator<Integer>() {

            public boolean validate(Integer response) throws Exception {
                
                return (response >= count);
            }
            
        };
    }
	
}