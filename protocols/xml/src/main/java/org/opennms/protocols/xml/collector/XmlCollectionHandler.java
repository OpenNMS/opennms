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
package org.opennms.protocols.xml.collector;

import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;

/**
 * The Class XML Collection Handler.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public interface XmlCollectionHandler  {

    /**
     * Collect.
     *
     * @param agent the collection agent
     * @param collection the XML collection configuration
     * @param parameters the collector parameters
     * @return the XML collection set
     * @throws CollectionException the collection exception
     */
    public CollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException;

    /**
     * Sets the RRD repository.
     *
     * @param repository the new RRD repository
     */
    public void setRrdRepository(RrdRepository repository);

    /**
     * Sets the service name associated with this Collection Handler.
     *
     * @param serviceName the new service name
     */
    public void setServiceName(String serviceName);

    public String parseUrl(final NodeDao nodeDao, final String unformattedUrl, final CollectionAgent agent, final Integer collectionStep, final Map<String, String> parameters) throws IllegalArgumentException;

    public Request parseRequest(final NodeDao nodeDao, final Request unformattedRequest, final CollectionAgent agent, final Integer collectionStep, final Map<String, String> parameters) throws IllegalArgumentException;

}
