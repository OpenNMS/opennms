/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 18, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
