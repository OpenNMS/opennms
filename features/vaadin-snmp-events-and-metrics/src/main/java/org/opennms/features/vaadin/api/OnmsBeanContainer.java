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
package org.opennms.features.vaadin.api;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.v7.data.util.BeanContainer;
import com.vaadin.v7.data.util.BeanItem;

/**
 * The Class OnmsBeanContainer.
 *
 * @param <T> the OpenNMS entity
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class OnmsBeanContainer<T> extends BeanContainer<Long, T> {

    /**
     * Instantiates a new OpenNMS bean container.
     *
     * @param type the type
     */
    public OnmsBeanContainer(Class<? super T> type) {
        super(type);
        setBeanIdResolver(new BeanIdResolver<Long,T>() {
            @Override
            public Long getIdForBean(T bean) {
                return generateItemId();
            }
        });
    }

    /**
     * Generates an itemId.
     *
     * @return the object
     */
    public Long generateItemId() {
        return System.nanoTime();
    }

    /**
     * Adds an OpenNMS bean.
     *
     * @param bean the new OpenNMS bean
     * @return the itemId
     */
    public Object addOnmsBean(T bean) {
        Long itemId = generateItemId();
        addItem(itemId, bean);
        return itemId;
    }

    /**
     * Gets the OpenNMS bean.
     *
     * @param itemId the item id
     * @return the OpenNMS bean
     */
    public T getOnmsBean(Object itemId) {
        BeanItem<T> item = getItem(itemId);
        return item == null ? null : item.getBean();
    }

    /**
     * Gets the OpenNMS beans.
     *
     * @return the OpenNMS beans
     */
    public List<T> getOnmsBeans() {
        List<T> beans = new ArrayList<>();
        for (Object itemId : getItemIds()) {
            beans.add(getOnmsBean(itemId));
        }
        return beans;
    }
}
