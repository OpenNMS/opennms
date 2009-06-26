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

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

public class PaletteTestUtils extends Assert {

	public static void assertPaletteItemEquals(PaletteItem expectedItem, PaletteItem actualItem) {
		assertEquals(expectedItem.getId(), actualItem.getId());
		assertEquals(expectedItem.getLabel(), actualItem.getLabel());
		assertEquals(expectedItem.isSpacer(), actualItem.isSpacer());
	}

	public static void assertPaletteCateriesEquals(List<PaletteCategory> expectedCategories, List<PaletteCategory> actualCategories) {
		if (expectedCategories == null) {
			assertNull(actualCategories);
			return;
		}
//		assertEquals(expectedCategories.size(), actualCategories.size());
		Iterator<PaletteCategory> iter = actualCategories.iterator();
		for (PaletteCategory expectedCategory : expectedCategories) {
			assertTrue(iter.hasNext());
			PaletteCategory actualCategory = iter.next();
			PaletteTestUtils.assertPaletteCategoryEquals(expectedCategory, actualCategory);
		}
		assertFalse(iter.hasNext());
	}

	public static void assertPaletteEquals(Palette expectedPalette, Palette actualPalette) {
		assertEquals(expectedPalette.getLabel(), actualPalette.getLabel());
		assertPaletteCateriesEquals(expectedPalette.getCategories(), actualPalette.getCategories());
	}

	public static void assertPaletteItemsEqual(List<PaletteItem> expectedItems, List<PaletteItem> actualItems) {
		if (expectedItems == null) {
			assertNull(actualItems);
			return;
		}
//		assertEquals(expectedItems.size(), actualItems.size());
		Iterator<PaletteItem> iter = actualItems.iterator();
		for (PaletteItem expectedItem : expectedItems) {
			assertTrue("More expected items than actual, Missing: "+expectedItem, iter.hasNext());
			PaletteItem actualItem = iter.next();
			assertPaletteItemEquals(expectedItem, actualItem);
		}
		
		if (iter.hasNext()) {
			fail("More actual items than expected, Found: "+iter.next());
		}
	}

	public static void assertPaletteCategoryEquals(PaletteCategory expectedCategory, PaletteCategory actualCategory) {
		assertEquals("Unexpected category label", expectedCategory.getLabel(), actualCategory.getLabel());
		assertPaletteItemsEqual(expectedCategory.getItems(), actualCategory.getItems());
	}

}
