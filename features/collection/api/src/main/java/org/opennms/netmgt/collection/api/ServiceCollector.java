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
package org.opennms.netmgt.collection.api;

import java.util.Map;

import org.opennms.netmgt.rrd.RrdRepository;

/**
 * Used to collect metrics from a {@link CollectionAgent} in
 * both OpenNMS and Minion.
 *
 * The order of the calls made to this interface depend on whether
 * we are running in OpenNMS or Minion.
 *
 * In OpenNMS when collecting in OpenNMS:
 *   * initialize()
 *   * getEffectiveLocation()
 *   * getRuntimeAttributes()
 *   * collect()
 *   * getRrdRepository()
 *
 * In OpenNMS when collection in Minion:
 *   * initialize()
 *   * getEffectiveLocation()
 *   * getRuntimeAttributes()
 *   * marshalParameters()
 *   * (RPC Invocation of collect())
 *   * getRrdRepository()
 *
 * In Minion:
 *   * unmarshalParameters()
 *   * collect()
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author jwhite
 */
public interface ServiceCollector {

    /**
    * Initialize the collector.
    *
    * This call will be invoked in OpenNMS before any other calls
    * to the collector are made.
    *
    * This function may be invoked several times during the lifetime
    * or the collector.
    *
    * @throws CollectionInitializationException
    */
   void initialize() throws CollectionInitializationException;

    /**
     * Validate whether or not this collector should be scheduled
     * to run against the given agent.
     *
     * If the collector cannot, or should not be a run against
     * a given agent, a {@link CollectionInitializationException}
     * must be thrown.
     *
     * In the case of the SNMP collector, this is used to prevent
     * collect from scheduling interfaces other than the those
     * marked as primary on a given node.
     *
     * @param agent
     * @param parameters
     * @throws CollectionInitializationException
     */
    void validateAgent(CollectionAgent agent, Map<String, Object> parameters) throws CollectionInitializationException;


    /**
     * Invokes a collection on the object.
     *
     * This call will be performed in both OpenNMS and Minion.
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param parameters a {@link java.util.Map} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionSet} object.
     * @throws org.opennms.netmgt.collection.api.CollectionException if any.
     */
    CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException;

    /**
     * Retrieve the {@link RrdRepository} configuration for the given collection.
     *
     * This call will always be performed in OpenNMS.
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    RrdRepository getRrdRepository(String collectionName);

    /**
    * Invoked before every call to {@link #collect(CollectionAgent, Map)} in order
    * to retrieve state/runtime information required for perform the collection.
    *
    * This call will always be performed in OpenNMS.
    *
    * @param agent
    *            Includes details about to the agent from which we wish to collect.
    * @param parameters
    *            Includes the service parameters defined in <em>collectd-configuration.xml</em>.
    * @return Additional attributes, which should be added to the parameter map before calling {@link #collect(CollectionAgent, Map)}.
    */
   Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters);

   /**
    * Allows the collector to override the location at which it should be run.
    *
    * This call will always be performed in OpenNMS.
    *
    * @param location
    *            location associated with the service to be monitored
    * @return a possibly updated location
    */
   String getEffectiveLocation(String location);

   /**
    * Marshal the parameter values to strings, which is necessary for
    * passing the parameters over the wire for the RPC call.
    *
    * We delegate this task to the {@link ServiceCollector} instead of handling
    * it elsewhere since the API, and RPC module do not have access
    * to the model objects used by the collector.
    *
    * This will only be called in OpenNMS when the collector is to be executed remotely.
    *
    * @param parameters
    * @return
    */
   Map<String, String> marshalParameters(Map<String, Object> parameters);

   /**
   * Unmarshal the parameter values from strings.
   *
   * This call will always be performed in Minion.
   *
   * @param parameters
   * @return
   */
   Map<String, Object> unmarshalParameters(Map<String, String> parameters);

}
