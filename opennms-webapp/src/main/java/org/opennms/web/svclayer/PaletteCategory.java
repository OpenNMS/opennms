/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>PaletteCategory class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class PaletteCategory {

	private String m_label;
	private List<PaletteItem> m_items = new LinkedList<PaletteItem>();

	/**
	 * <p>Constructor for PaletteCategory.</p>
	 */
	public PaletteCategory() {
		this(null);
	}
	
	/**
	 * <p>Constructor for PaletteCategory.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public PaletteCategory(String label) {
		m_label = label;
	}

	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLabel() {
		return m_label;
	}
	
	/**
	 * <p>setLabel</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		m_label = label;
	}
	
	/**
	 * <p>getItems</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<PaletteItem> getItems() {
		return Collections.unmodifiableList(m_items);
	}
	
	/**
	 * <p>addItem</p>
	 *
	 * @param item a {@link org.opennms.web.svclayer.PaletteItem} object.
	 */
	public void addItem(PaletteItem item) {
		m_items.add(item);
	}
	
	
}
