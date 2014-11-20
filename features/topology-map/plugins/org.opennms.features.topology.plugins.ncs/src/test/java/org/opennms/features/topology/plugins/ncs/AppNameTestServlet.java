/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.ncs;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AppNameTestServlet extends HttpServlet {
    
    private String m_responseString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" +
    		"<Data xmlns=\"services.schema.networkapi.jmp.juniper.net\" >" +
    		"<ServiceResource>\n" +
    		"<Service href=\"/api/space/nsas/eline-ptp/service-management/services/884779\" uri=\"/api/space/nsas/eline-ptp/service-management/services/884779\" key=\"884779\">" +
    		"<Common>" +
    		"<Name>testpw3</Name>" +
    		"<Identity>884779</Identity>" +
    		"<State>Deployed</State>" +
    		"<CreatedDate>2013-04-16T18:43:11.000Z</CreatedDate>" +
    		"<LastUpdatedDate>2013-04-16T18:43:11.000Z</LastUpdatedDate>" +
    		"</Common>" +
    		"<ServiceType>eline-ptp</ServiceType>" +
    		"<AuditFlag>" +
    		"<FunctionalAudit>Up</FunctionalAudit>" +
    		"<FaultStatus>None</FaultStatus>" +
    		"</AuditFlag>" +
    		"<Reference>" +
    		"<ServiceOrder key=\"884772\" href=\"/api/space/nsas/eline-ptp/service-management/service-orders/884772\" uri=\"/api/space/nsas/eline-ptp/service-management/service-orders/884772\"/>" +
    		"<ServiceDefinition>" +
    		"<ServiceDefinitionID key=\"393216\" href=\"/api/space/nsas/eline-ptp/service-management/service-definitions/393216\" uri=\"/api/space/nsas/eline-ptp/service-management/service-definitions/393216\"/>" +
    		"</ServiceDefinition>" +
    		"<Customer key=\"491520\" href=\"/api/space/nsas/customer-management/customers/491520\" uri=\"/api/space/nsas/customer-management/customers/491520\"/>" +
    		"</Reference>" +
    		"</Service>" +
    		"</ServiceResource>" +
    		"</Data>";
    
    private String m_responseString2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
    		"<Data xmlns=\"services.schema.networkapi.jmp.juniper.net\">" +
    		"<ServiceResource>" +
    		"<Service href=\"/api/space/nsas/eline-ptp/service-management/services/491769\" uri=\"/api/space/nsas/eline-ptp/service-management/services/491769\" key=\"491769\">" +
    		"<Common>" +
    		"<Name>qinq_p2p</Name>" +
    		"<Identity>491769</Identity>" +
    		"<State>Deployed</State>" +
    		"<CreatedDate>2013-05-28T12:27:40.000Z</CreatedDate>" +
    		"<LastUpdatedDate>2013-05-28T12:27:40.000Z</LastUpdatedDate>" +
    		"</Common>" +
    		"<ServiceType>eline-ptp</ServiceType>" +
    		"<AuditFlag>" +
    		"<FunctionalAudit>Up</FunctionalAudit>" +
    		"<FaultStatus>None</FaultStatus>" +
    		"</AuditFlag>" +
    		"<Reference>" +
    		"<ServiceOrder key=\"491762\" href=\"/api/space/nsas/eline-ptp/service-management/service-orders/491762\" uri=\"/api/space/nsas/eline-ptp/service-management/service-orders/491762\"/>" +
    		"<ServiceDefinition>" +
    		"<ServiceDefinitionID key=\"491520\" href=\"/api/space/nsas/eline-ptp/service-management/service-definitions/491520\" uri=\"/api/space/nsas/eline-ptp/service-management/service-definitions/491520\"/>" +
    		"</ServiceDefinition>" +
    		"<Customer key=\"590215\" href=\"/api/space/nsas/customer-management/customers/590215\" uri=\"/api/space/nsas/customer-management/customers/590215\"/>" +
    		"</Reference>" +
    		"</Service>" +
    		"</ServiceResource>" +
    		"</Data>";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/xml");
        PrintWriter out = resp.getWriter();
        out.write(m_responseString);
        //out.write(m_responseString2);
    }

}
