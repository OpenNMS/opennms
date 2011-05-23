//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.invd;

import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.config.invd.InvdService;
import org.opennms.netmgt.config.invd.InvdServiceParameter;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.netmgt.invd.exceptions.InventoryException;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.core.utils.ThreadCategory;

import java.util.Map;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Collection;

public class ScannerSpecification {
    private InvdPackage m_package;
    private String m_svcName;
    private InventoryScanner m_scanner;
    private Map<String, String> m_parameters;

    public ScannerSpecification(InvdPackage wpkg, String svcName, InventoryScanner scanner) {
        m_package = wpkg;
        m_svcName = svcName;
        m_scanner = scanner;

        initializeParameters();
    }

    public String getPackageName() {
        return m_package.getName();
    }

    private InvdService getService() {
        return m_package.getServiceByName(m_svcName);
    }

    public String getServiceName() {
        return m_svcName;
    }

    private void setPackage(InvdPackage pkg) {
        m_package = pkg;
    }

    public long getInterval() {
        return getService().getInterval();

    }

    public String toString() {
        return m_svcName + '/' + m_package.getName();
    }

    private InventoryScanner getScanner() {
        return m_scanner;
    }

    private Map<String, String> getPropertyMap() {
        return m_parameters;
    }

    /**
     * Return a read only instance of the parameters, which consists of the overall service parameters,
     * plus various other Collection specific parameters (e.g. storeByNodeID etc)
     * @return A read only Map instance
     */
    public Map<String, String> getReadOnlyPropertyMap() {
        return Collections.unmodifiableMap(m_parameters);
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    @SuppressWarnings("unused")
	private boolean isTrue(String stg) {
        return stg.equalsIgnoreCase("yes") || stg.equalsIgnoreCase("on") || stg.equalsIgnoreCase("true");
    }

    @SuppressWarnings("unused")
	private boolean isFalse(String stg) {
        return stg.equalsIgnoreCase("no") || stg.equalsIgnoreCase("off") || stg.equalsIgnoreCase("false");
    }

    private void initializeParameters() {
        Map<String, String> m = new TreeMap<String, String>();
        m.put("SERVICE", m_svcName);
        StringBuffer sb;
        Collection<InvdServiceParameter> params = getService().getServiceParameters();
        for (InvdServiceParameter p : params) {
            if (log().isDebugEnabled()) {
                sb = new StringBuffer();
                sb.append("initializeParameters: adding service: ");
                sb.append(getServiceName());
                sb.append(" parameter: ");
                sb.append(p.getKey());
                sb.append(" of value ");
                sb.append(p.getValue());
                log().debug(sb.toString());
            }
            m.put(p.getKey(), p.getValue());
        }
        
        m_parameters = m;
    }

    public void refresh(InvdConfigDao invdConfigDao) {
        InvdPackage refreshedPackage = invdConfigDao.getPackage(getPackageName());
        if (refreshedPackage != null) {
            setPackage(refreshedPackage);
        }
    }

    public InventorySet collect(ScanningClient agent) throws InventoryException {
        //Collectd.instrumentation().beginCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            return getScanner().collect(agent, eventProxy(), getPropertyMap());
        } finally {
            ;
            //Collectd.instrumentation().endCollectorCollect(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    private EventProxy eventProxy() {
        return new EventProxy() {
            public void send(Event e) {
                EventIpcManagerFactory.getIpcManager().sendNow(e);
            }

            public void send(Log log) {
                EventIpcManagerFactory.getIpcManager().sendNow(log);
            }
        };
    }

    public boolean scheduledOutage(ScanningClient agent) {
        boolean outageFound = false;

        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();

        /*
         * Iterate over the outage names defined in the interface's package.
         * For each outage...if the outage contains a calendar entry which
         * applies to the current time and the outage applies to this
         * interface then break and return true. Otherwise process the
         * next outage.
         */
        for (String outageName : m_package.getOutageCalendars()) {
            // Does the outage apply to the current time?
            if (outageFactory.isCurTimeInOutage(outageName)) {
                // Does the outage apply to this interface?
                if ((outageFactory.isNodeIdInOutage(agent.getNodeId(), outageName)) ||
                        (outageFactory.isInterfaceInOutage(agent.getHostAddress(), outageName)))
                {
                    if (log().isDebugEnabled()) {
                        log().debug("scheduledOutage: configured outage '" + outageName + "' applies, interface " + agent.getHostAddress() + " will not be collected for " + this);
                    }
                    outageFound = true;
                    break;
                }
            }
        }

        return outageFound;
    }

    public void initialize(ScanningClient agent) {
        //Collectd.instrumentation().beginCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_scanner.initialize(agent, getPropertyMap());
        } finally {
            ; // nothing here yet.
            //Collectd.instrumentation().endCollectorInitialize(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }

    public void release(ScanningClient agent) {
        //Collectd.instrumentation().beginCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        try {
            m_scanner.release(agent);
        } finally {
            ; // nothing here yet
            //Collectd.instrumentation().endCollectorRelease(agent.getNodeId(), agent.getHostAddress(), m_svcName);
        }
    }
}
