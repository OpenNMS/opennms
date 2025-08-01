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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.AbstractBeanContainer;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.MethodPropertyDescriptor;
import com.vaadin.v7.data.util.ObjectProperty;
import com.vaadin.v7.data.util.PropertysetItem;
import com.vaadin.v7.data.util.VaadinPropertyDescriptor;

/**
 * This class represents a selectable item. In encapsulates any java objects
 * which fulfillths the java bean convention. In addition it implements
 * {@link Selectable} which determines that this item can be selected. This
 * class is mainly a rough copy of {@link com.vaadin.v7.data.util.BeanItem}, but
 * due to some limitations in how the {@link BeanItemContainer} (or
 * {@link AbstractBeanContainer}) handles the creation of a BeanItem I decided
 * to implement a new SelectableItem. This may change in future releases in the
 * vaadin core framework. So we may remove this item.
 * 
 * @param <T>
 *            The type of the Java Bean we want to use as a
 *            {@linkplain SelectableItem}
 * 
 * @author Markus von Rüden
 * @see BeanItem
 */
public class SelectableItem<T> extends PropertysetItem implements Selectable {

	private static final Logger LOG = LoggerFactory.getLogger(SelectableItem.class);

	/**
	 * The bean to store.
	 */
	private final T bean;

	SelectableItem(T bean, Map<String, VaadinPropertyDescriptor> pdMap) {
		this.bean = bean;
		for (VaadinPropertyDescriptor pd : pdMap.values())
			addItemProperty(pd.getName(), pd.createProperty(bean));
	}

	public SelectableItem(T bean) {
		this(bean, getPropertyDescriptors((Class<? super T>) bean.getClass()));
	}

	@Override
	public boolean isSelected() {
		return (Boolean) getItemProperty("selected").getValue();
	}

	public void setSelected(boolean select) {
		getItemProperty("selected").setValue(Boolean.valueOf(select));
	}

	public T getBean() {
		return bean;
	}

	/**
	 * <b>Note:</b>This method is simmilar to
	 * {@link com.vaadin.v7.data.util.BeanItem#getPropertyDescriptors(java.lang.Class)
	 * }
	 * but adds an additional <code>VaadinPropertyDescriptor</codE> to support a
	 * "is selectable" feature. Because the earlier mentioned method is static,
	 * we cannot overwrite it. Therefore I decided to implement it in my own
	 * way.<br/>
	 * <br/>
	 * 
	 * <b>In short:</b> It lookups all methods which fullfill the java bean
	 * convention of <code>clazz</code> and builds accessors for it (read
	 * method, write method, etc. for a field, etc. Have a look at
	 * {@link java.beans.Introspector}). And in addition we a new "accessor" for
	 * a selectable item is added.<br/>
	 * <br/>
	 * 
	 * <b>In detail:</b><br/>
	 * {@link java.beans.Introspector} is used to get all PropertyDescriptors
	 * from the given class <code>clazz</code>. Each PropertyDescriptor is
	 * converted to a vaadin <code>MethodPropertyDescriptor</code>(
	 * {@link com.vaadin.v7.data.util.MethodPropertyDescriptor} is created. In
	 * addition a VaadinPropertyDescriptor is added to provide the "select"
	 * feature. For this we use the <code>ObjectProperty</code> of vaadin. <br/>
	 * <br/>
	 * 
	 * The build map is a mapping between property names and the
	 * <code>VaadinPropertyDescriptor</code>s, whereby
	 * {@link com.vaadin.v7.data.util.VaadinPropertyDescriptor#getName()} is
	 * identically with the key of the returned map (and therefore represents
	 * the property name).
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	protected static <T> Map<String, VaadinPropertyDescriptor> getPropertyDescriptors(Class<? super T> clazz) {
		Map<String, VaadinPropertyDescriptor> mpdMap = new HashMap<String, VaadinPropertyDescriptor>();
		try {
			// add all available method property descriptors
			for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
				MethodPropertyDescriptor mpd = new MethodPropertyDescriptor<T>(pd.getName(), pd.getPropertyType(),
						pd.getReadMethod(), pd.getWriteMethod());
				mpdMap.put(pd.getName(), mpd);
			}
			// add selected property descriptor
			mpdMap.put("selected", new VaadinPropertyDescriptor<T>() {
				@Override
				public String getName() {
					return "selected";
				}

				@Override
				public Class<?> getPropertyType() {
					return Boolean.class;
				}

				@Override
				public Property createProperty(T value) {
					return new ObjectProperty(false, Boolean.class, false);
				}
			});
		} catch (IntrospectionException ex) {
			LOG.warn("Error while introspecting class '{}'. Will continue anyway, result may not be complete.", clazz);
		}
		return mpdMap;
	}
}
