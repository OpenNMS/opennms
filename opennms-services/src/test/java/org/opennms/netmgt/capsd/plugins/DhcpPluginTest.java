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
package org.opennms.netmgt.capsd.plugins;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.config.DhcpdConfigFactory;
import org.opennms.netmgt.dhcpd.Dhcpd;


/**
 * @author thedesloge
 *
 */
public class DhcpPluginTest {
    
    private Dhcpd m_dhcpd;
    private Thread m_dhcpdThread;
    
    @Before
    public void setup() throws Exception{
        setDhcpdThread((new Thread(getRunnable())));
        getDhcpdThread().start();
    }
    
    @After
    public void tearDown(){
        if(getDhcpdThread() != null){
            getDhcpdThread().stop();
        }
    }
    
    @Ignore
    @Test
    public void testPlugin() throws MarshalException, ValidationException, IOException {
        DhcpdConfigFactory.init();
        DhcpPlugin plugin = new DhcpPlugin();

        assertTrue(plugin.isProtocolSupported(InetAddress.getByName("192.168.1.1")));
    }
    
    protected static Runnable getRunnable() throws Exception {
        return new Runnable(){
            
            public void run(){
                Dhcpd dhcpdDeamon = Dhcpd.getInstance();
                try{
                    dhcpdDeamon.init();
                    dhcpdDeamon.start();
                    dhcpdDeamon.run();
                }catch(Exception e){
                    e.printStackTrace();
                } finally {
                    dhcpdDeamon.stop();
                }
            }
            
        };
    }

    public void setDhcpdThread(Thread dhcpdThread) {
        m_dhcpdThread = dhcpdThread;
    }

    public Thread getDhcpdThread() {
        return m_dhcpdThread;
    }
}
