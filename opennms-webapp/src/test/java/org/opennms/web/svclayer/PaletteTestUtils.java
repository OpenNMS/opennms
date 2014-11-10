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

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

public abstract class PaletteTestUtils extends Assert {

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
