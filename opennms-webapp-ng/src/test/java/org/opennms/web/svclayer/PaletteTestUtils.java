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
