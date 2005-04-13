/**
 * 
 */
package org.opennms.web.jWebUnitTests;

import java.util.ArrayList;
import java.util.List;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

import net.sourceforge.jwebunit.WebTestCase;

/**
 * @author mhuot
 *
 */
public class OpenNMSWebTestCase extends WebTestCase {

    private String[] m_menu = { "Node List", "Search", "Outages", "Events", "Notification", "Assets", "Reports", "Help" };

    protected void assertHeaderPresent(String title, String location, String[] breadcrumbs) {
    
        assertTablePresent("header");
    
        WebTable headertable = getDialog().getWebTableBySummaryOrId("header");
        
        // ensure the logo image is there
        assertCellImage(headertable, 0, 0,  null);
        
        // ensure the title is correct 
        assertCell(headertable, 0, 1, title);
        
        //Second line had a table in it that spans the three columns, we call it sub-header
    
        //Chect that the sub-header table is present
        assertTablePresent("sub-header");
        
        WebTable subheadertable = getDialog().getWebTableBySummaryOrId("sub-header");
        
        // Ensure the bread crumbs are correct
        assertBreadCrumbs(breadcrumbs, subheadertable.getTableCell(0,0));
    
        // Ensure the menu links are correct
        // TODO: Fix Admin right now coerce it to Null since we don't have auth right
        assertMenu(("Admin".equals(location) ?  null : location), m_menu, subheadertable.getTableCell(0,1));
    }

    private void assertBreadCrumbs(String[] breadcrumbs, TableCell cell) {
        if (breadcrumbs != null && breadcrumbs.length > 0)
            assertMenu(breadcrumbs[breadcrumbs.length-1], breadcrumbs, cell);
    }

    protected void assertFooterPresent(String location) {
        assertTablePresent("footer");
        WebTable table = getDialog().getWebTableBySummaryOrId("footer");
        TableCell cell = table.getTableCell(0, 0);
    
        // TODO: Fix Admin right now coerce it to Null since we don't have auth right
        assertMenu(("Admin".equals(location) ?  null : location), m_menu, cell);
        assertCell(table, 1, 0, "OpenNMS Copyright \u00a9 2002-2005 The OpenNMS Group, Inc. OpenNMS\u00ae is a registered trademark of The OpenNMS Group, Inc.");
    }

    private void assertMenu(String location, String[] menu, TableCell cell) {
        if (location != null)
            assertTrue("Expected disabled menu item "+location+" but the cell is "+cell.getText(), cell.getText().indexOf(location) >= 0);
        List links = new ArrayList();
        for(int i = 0; i < menu.length; i++) {
            if (!menu[i].equals(location)) {
                links.add(menu[i]);
            }
        }
        assertLinks((String[]) links.toArray(new String[links.size()]), cell.getLinks());
    }

    public void assertCellImage(WebTable table, int row, int col, String imgSrc) {
        assertEquals(1, table.getTableCell(row, col).getImages().length);
        WebImage img = table.getTableCell(row, col).getImages()[0];
        assertNotNull(img);
        if (imgSrc != null)
            assertEquals(imgSrc, img.getSource());
    }

    private void assertLinks(String[] text, WebLink[] links) {
        for (int i = 0; i < text.length; i++) {
            assertTrue("Missing Link '"+text[i]+"'", i < links.length);
            WebLink link = links[i];
            assertNotNull(link);
            assertEquals("Missing Link '"+text[i]+"'", text[i], link.getText());
        }
        
        if (text.length < links.length) {
            fail((links.length - text.length)+" unexpected links starting at '"+links[text.length].getText()+"'");
        }
    }

    public void assertCell(WebTable table, int row, int col, String contents, int colspan) {
        if (contents == null) {
            assertNull(table.getTableCell(row, col));
        } else {
            assertNotNull(table.getTableCell(row, col));
            assertEquals(contents, table.getCellAsText(row, col));
            assertEquals(colspan, table.getTableCell(row, col).getColSpan());
        }
    }

    public void assertCell(WebTable table, int row, int col, String contents) {
        assertCell(table, row, col, contents, 1);
    }


}
