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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.jmxconfiggenerator.data.SelectableBeanItemContainer;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.Mbean;

/**
 * The MBeanTree shows all available MBeans. Each Mbean has one or more
 * attributes. Each attribute is selectable. The MBean's attributes are
 * shown in a table. The problem is, that we must store the "is selected"
 * state of each AttributeItem. So we have two choices:<br/>
 *
 * 1. add ALL attributes from ALL MBeans to the container of the table and
 * show only the one belonging to the selected Mbean.<br/>
 *
 * 2. only add selected MBean's attributes to the container and save the
 * container for later use.<br/>
 *
 * We stick to 2. So at the beginning this class simply maps each MBean to
 * its container. But further on we realized that there are more scenarios
 * where we have a parent object which has a list of attributes. So the
 * {@link AttributesContainerCache} got more generic. Therefore the
 * ATTRIBUTETYPE defines the type of the attribute (e.g. {@link Attrib} to
 * stick with the MBeans example) and the PARENTTYPE defines the type of the
 * parent object (e.g. {@link Mbean} to stick with the MBeans example).
 *
 * @param <ATTRIBUTETYPE>
 *            The type of the parent object's attributes.
 * @param <PARENTTYPE>
 *            The type of the parent object which holds the attributes.
 *
 * @author Markus von Rüden
 */
public class AttributesContainerCache<ATTRIBUTETYPE, PARENTTYPE> {

    /**
     * The AttributeCollector retrieves all attributes from the parent's
     * object.
     *
     * @author Markus von Rüden
     *
     * @param <ATTRIBUTETYPE>
     *            The type of the attributes.
     * @param <PARENTTYPE>
     *            The type of the parent's object.
     */
    public interface AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> {

        /**
         * Retrieves all attributes from the parent's object. Usually should
         * do something like <code>return parent.getChildren()</code>
         *
         * @param parent
         *            The parent object.
         * @return all attributes from the parent's object.
         */
        List<ATTRIBUTETYPE> getAttributes(PARENTTYPE parent);
    }

    /**
     * The map to map the container to the parent's object.
     */
    private final Map<PARENTTYPE, SelectableBeanItemContainer<ATTRIBUTETYPE>> containerMap = new HashMap<PARENTTYPE, SelectableBeanItemContainer<ATTRIBUTETYPE>>();

    /**
     * The type of the attribute.
     */
    private final Class<? super ATTRIBUTETYPE> type;

    /**
     * The collector to get all Attributes from, e.g. to get all Attributes from a MBean.
     */
    private final AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> attribCollector;

    AttributesContainerCache(Class<? super ATTRIBUTETYPE> type,
							 AttributeCollector<ATTRIBUTETYPE, PARENTTYPE> attribCollector) {
        this.type = type;
        this.attribCollector = attribCollector;
    }

    /**
     * Gets the container of the given bean. If there is no container a new
     * one is created, otherwise the earlier used container is returned.
     *
     * @param bean
     * @return
     */
    public SelectableBeanItemContainer<ATTRIBUTETYPE> getContainer(PARENTTYPE bean) {
        if (bean == null) return null;
        if (containerMap.get(bean) != null) return containerMap.get(bean);
        containerMap.put(bean, new SelectableBeanItemContainer<>(type));
        initContainer(containerMap.get(bean), bean);
        return containerMap.get(bean);
    }

    public PARENTTYPE getParentFor(ATTRIBUTETYPE child) {
        for (Map.Entry<PARENTTYPE, SelectableBeanItemContainer<ATTRIBUTETYPE>> eachEntry : containerMap.entrySet()) {
            if (eachEntry.getValue().containsId(child)) {
                return eachEntry.getKey();
            }
        }
        return null;
    }

    /**
     * Initializes the container. So the container must not be null. It simply adds all attributes to the container.
     * @param container The container.
     * @param bean The parent bean.
     */
    private void initContainer(SelectableBeanItemContainer<ATTRIBUTETYPE> container, PARENTTYPE bean) {
        for (ATTRIBUTETYPE att : attribCollector.getAttributes(bean)) {
            container.addItem(att);
        }
    }

    /**
     * Clears the containerMap.
     */
    public void clear() {
        containerMap.clear();
    }
}
