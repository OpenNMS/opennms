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
 * Created: July 20, 2006
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

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PaletteBuilder {
	
	private Palette m_palette;
	private PaletteCategory m_currentCategory;
	
	public PaletteBuilder(String label) {
		m_palette = new Palette(label);
	}
	
	public PaletteBuilder addCategory(String label) {
		m_currentCategory = new PaletteCategory(label);
		m_palette.addCategory(m_currentCategory);
		return this;
	}
	
	public PaletteBuilder addItem(String id, String label) {
		PaletteItem item = new PaletteItem(id, label);
		m_currentCategory.addItem(item);
		return this;
	}
	
	public PaletteBuilder addSpacer() {
		m_currentCategory.addItem(PaletteItem.SPACER);
		return this;
	}
	
	public Palette getPalette() {
		return m_palette;
	}
	
	

}
