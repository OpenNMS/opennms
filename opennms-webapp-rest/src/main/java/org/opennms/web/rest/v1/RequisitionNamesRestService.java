/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.text.ParseException;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.web.svclayer.api.RequisitionAccessService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class RequisitionNamesRestService.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("requisitionNamesRestService")
@Path("requisitionNames")
public class RequisitionNamesRestService extends OnmsRestService {

    /** The m_access service. */
    @Autowired
    private RequisitionAccessService m_accessService;

    /**
     * The Class RequisitionCollection.
     */
    @SuppressWarnings("serial")
    @XmlRootElement(name="foreign-sources")
    public static class RequisitionCollection extends JaxbListWrapper<String> {

        /**
         * Gets the names.
         *
         * @return the names
         */
        @XmlElement(name="foreign-source")
        public List<String> getNames() {
            return getObjects();
        }
    }

    /**
     * Tear down.
     */
    @PreDestroy
    protected void tearDown() {
        if (m_accessService != null) {
            m_accessService.flushAll();
        }
    }

    /**
     * Gets the requisition names.
     *
     * @return the requisition names
     * @throws ParseException the parse exception
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public RequisitionCollection getRequisitionNames() throws ParseException {
        RequisitionCollection names = new RequisitionCollection();
        m_accessService.getRequisitions().forEach(r -> names.add(r.getForeignSource()));
        return names;
    }

}
