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

import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "RequisitionNames", description = "Requisition Names API")
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
