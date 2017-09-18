/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import java.util.Map;

import org.opennms.netmgt.events.api.EventProxy;
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
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param parameters a {@link java.util.Map} object.
     * @return a {@link org.opennms.netmgt.config.collector.CollectionSet} object.
     * @throws org.opennms.netmgt.collectd.CollectionException if any.
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
    * Invoked before every call to {@link #collect(CollectionAgent, EventProxy, Map)} in order
    * to retrieve state/runtime information required for perform the collection.
    *
    * This call will always be performed in OpenNMS.
    *
    * @param agent
    *            Includes details about to the agent from which we wish to collect.
    * @param parameters
    *            Includes the service parameters defined in <em>collectd-configuration.xml</em>.
    * @return Additional attributes, which should be added to the parameter map before calling {@link #collect(CollectionAgent, EventProxy, Map)}.
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
