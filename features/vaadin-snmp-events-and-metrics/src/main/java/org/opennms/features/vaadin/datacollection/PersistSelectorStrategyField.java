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
package org.opennms.features.vaadin.datacollection;

import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;

/**
 * The Persist Selector Strategy Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class PersistSelectorStrategyField extends AbstractStrategyField<PersistenceSelectorStrategy> {

    /**
     * Instantiates a new persist selector strategy field.
     *
     * @param caption the caption
     */
    public PersistSelectorStrategyField(String caption) {
        // If the strategy from the XML is different, it will be added automatically to the combo-box
        // The following list is not using class references to avoid add a dependency against opennms-services
        super(caption, new String[] {
                "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy",
                "org.opennms.netmgt.collectd.PersistRegexSelectorStrategy"
        });
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<PersistenceSelectorStrategy> getType() {
        return PersistenceSelectorStrategy.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(PersistenceSelectorStrategy strategy) {
        setComboValue(strategy.getClazz());
        container.removeAllItems();
        container.addAll(strategy.getParameters());
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected PersistenceSelectorStrategy getInternalValue() {
        PersistenceSelectorStrategy strategy = new PersistenceSelectorStrategy();
        if (combo.getValue() != null) {
            strategy.setClazz((String) combo.getValue());
        }
        for (Object itemId: container.getItemIds()) {
            strategy.addParameter(container.getItem(itemId).getBean());
        }
        return strategy;
    }

}
