/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.data;

import com.vaadin.data.util.AbstractBeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodPropertyDescriptor;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.util.VaadinPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a selectable item. In encapsulates any java objects
 * which fulfillths the java bean convention. In addition it implements
 * {@link Selectable} which determines that this item can be selected. This
 * class is mainly a rough copy of {@link com.vaadin.data.util.BeanItem}, but
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

	public T getBean() {
		return bean;
	}

	/**
	 * <b>Note:</b>This method is simmilar to
	 * {@link com.vaadin.data.util.BeanItem#getPropertyDescriptors(java.lang.Class)
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
	 * {@link com.vaadin.data.util.MethodPropertyDescriptor} is created. In
	 * addition a VaadinPropertyDescriptor is added to provide the "select"
	 * feature. For this we use the <code>ObjectProperty</code> of vaadin. <br/>
	 * <br/>
	 * 
	 * The build map is a mapping between property names and the
	 * <code>VaadinPropertyDescriptor</code>s, whereby
	 * {@link com.vaadin.data.util.VaadinPropertyDescriptor#getName()} is
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
				public com.vaadin.data.Property createProperty(T value) {
					return new ObjectProperty(true, Boolean.class, false);
				}
			});
		} catch (IntrospectionException ex) {
			;
		}
		return mpdMap;
	}
}
