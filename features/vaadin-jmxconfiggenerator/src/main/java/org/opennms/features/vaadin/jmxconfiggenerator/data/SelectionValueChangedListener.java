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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

/**
 * Listener to deal with "selection" value has changed.
 * This usually is the case when the Property "selected" of an item has changed.
 *
 */
public interface SelectionValueChangedListener {

    /**
     * The event to transport the necessary information
     */
    class SelectionValueChangedEvent {


        private Object itemId;
        private boolean newValue;
        private Object bean;

        public void setItemId(Object itemId) {
            this.itemId = itemId;
        }

        public Object getItemId() {
            return itemId;
        }

        public void setNewValue(boolean newValue) {
            this.newValue = newValue;
        }

        public boolean getNewValue() {
            return newValue;
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }

        public Object getBean() {
            return bean;
        }
    }

    /**
     * Is invoked AFTER the selection value has changed.
     *
     * @param selectionValueChangedEvent The event.
     */
    void selectionValueChanged(SelectionValueChangedEvent selectionValueChangedEvent);
}
