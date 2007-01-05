//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.svclayer;

import junit.framework.TestCase;

import static org.opennms.web.svclayer.PaletteTestUtils.assertPaletteEquals;

public class PaletteBuilderTest extends TestCase {
	
	private Palette m_palette;
	private PaletteCategory m_currentCat;

	public void testBuildingPalatte() {
		
		m_palette = new Palette("paletteLabel");
		PaletteBuilder m_builder = new PaletteBuilder("paletteLabel");
		
		m_builder.addCategory("cat1");
		createCategory("cat1");
		m_builder.addItem("c1a_id", "c1a_label");
		createItem("c1a_id", "c1a_label");
		m_builder.addItem("c1b_id", "c1b_label");
		createItem("c1b_id", "c1b_label");
		m_builder.addItem("c1c_id", "c1c_label");
		createItem("c1c_id", "c1c_label");
		
		m_builder.addCategory("cat2");
		createCategory("cat2");
		m_builder.addItem("c2a_id", "c2a_label");
		createItem("c2a_id", "c2a_label");
		m_builder.addSpacer();
		createSpacer();
		m_builder.addItem("c2b_id", "c2b_label");
		createItem("c2b_id", "c2b_label");
		
		assertPaletteEquals(m_palette, m_builder.getPalette());
		
		
	}

	private void createSpacer() {
		m_currentCat.addItem(PaletteItem.SPACER);
	}

	private void createCategory(String label) {
		PaletteCategory cat = new PaletteCategory(label);
		m_palette.addCategory(cat);
		m_currentCat = cat;
	}
	
	private void createItem(String id, String label) {
		PaletteItem item = new PaletteItem(id, label);
		m_currentCat.addItem(item);
	}
	
	
	

}
