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

/**
 * <p>PaletteBuilder class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class PaletteBuilder {
	
	private Palette m_palette;
	private PaletteCategory m_currentCategory;
	
	/**
	 * <p>Constructor for PaletteBuilder.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public PaletteBuilder(String label) {
		m_palette = new Palette(label);
	}
	
	/**
	 * <p>addCategory</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.svclayer.PaletteBuilder} object.
	 */
	public PaletteBuilder addCategory(String label) {
		m_currentCategory = new PaletteCategory(label);
		m_palette.addCategory(m_currentCategory);
		return this;
	}
	
	/**
	 * <p>addItem</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 * @return a {@link org.opennms.web.svclayer.PaletteBuilder} object.
	 */
	public PaletteBuilder addItem(String id, String label) {
		PaletteItem item = new PaletteItem(id, label);
		m_currentCategory.addItem(item);
		return this;
	}
	
	/**
	 * <p>addSpacer</p>
	 *
	 * @return a {@link org.opennms.web.svclayer.PaletteBuilder} object.
	 */
	public PaletteBuilder addSpacer() {
		m_currentCategory.addItem(PaletteItem.SPACER);
		return this;
	}
	
	/**
	 * <p>getPalette</p>
	 *
	 * @return a {@link org.opennms.web.svclayer.Palette} object.
	 */
	public Palette getPalette() {
		return m_palette;
	}
	
	

}
