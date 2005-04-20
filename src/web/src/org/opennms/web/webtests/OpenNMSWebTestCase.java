//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.web.webtests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jwebunit.WebTestCase;

import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebImage;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebTable;

public class OpenNMSWebTestCase extends WebTestCase {

    private String[] m_menu = { "Node List", "Search", "Outages", "Events", "Notification", "Assets", "Reports", "Admin", "Help" };

    /**
     * This method checks for the header to see if it is present, has the correct title, 
     * path based navigation menu on the left and the primary menu on the right.
     * 
     * @param title
     *            This is the title of the web page being checked
     * @param location
     *            Location is the current place with in the hierarchy of the menu that the 
     *            web page being checked is at.  The location will be deactivated as a 
     *            navigation option.
     * @param breadcrumbs
     *            The breadcrumbs are used to construct the path menu.  On the righthand side of the header.          
     */
    protected void assertHeaderPresent(String title, String location, String[] breadcrumbs) {
    
        assertTablePresent("header");
    
        WebTable headertable = getDialog().getWebTableBySummaryOrId("header");
        
        // ensure the logo image is there
        assertCellImage(headertable.getTableCell(0,0),  null);
        
        // ensure the title is correct 
        assertCell(headertable.getTableCell(0,1), title);
        
        //Second line has a table in it that spans the three columns, we call it sub-header
    
        //Chect that the sub-header table is present
        assertTablePresent("sub-header");
        
        WebTable subheadertable = getDialog().getWebTableBySummaryOrId("sub-header");
        
        // Ensure the bread crumbs are correct
        assertBreadCrumbs(breadcrumbs, subheadertable.getTableCell(0,0));
    
        // Ensure the menu links are correct
        assertMenu(location, m_menu, subheadertable.getTableCell(0,1));
    }

    /**
     * This method checks to see that the cell containing the breadcrumbs is correctly generated.  The cell
     * should contain the strings with all except the last being links.  The last option will look like a deactivated
     * link.
     * 
     * @param breadcrumbs
     *            The breadcrumbs are used to construct the path menu.  On the right hand side of the header.
     * @param cell
     *            The cell being checked for the correct breadcrumb links
     *           
     */
    private void assertBreadCrumbs(String[] breadcrumbs, TableCell cell) {
        if (breadcrumbs != null && breadcrumbs.length > 0)
            assertMenu(breadcrumbs[breadcrumbs.length-1], breadcrumbs, cell);
    }

    /**
     * This method checks for the footer to see if it is present and that the menu correctly displayed.  The menu should
     * have the current location deactived.
     * 
     * @param location
     *            Location is the current place with in the hierarchy of the menu that the 
     *            web page being checked is at.  The location will be deactivated as a 
     *            navigation option.
     *            
     */
    protected void assertFooterPresent(String location) {
        assertTablePresent("footer");
        WebTable table = getDialog().getWebTableBySummaryOrId("footer");
        TableCell cell = table.getTableCell(0, 0);
    
        assertMenu(location, m_menu, cell);
        assertCell(table.getTableCell(1, 0), "OpenNMS Copyright \u00a9 2002-2005 The OpenNMS Group, Inc. OpenNMS\u00ae is a registered trademark of The OpenNMS Group, Inc.");
    }

    /**
     * This method checks that a menu has the correct format.  All of the parts of the menu are links except the entry that 
     * matches location.  The location is included in the cell, but is deactivated.
     * 
     * @param location
     *            The breadcrumbs are used to construct the path menu.  On the righthand side of the header.
     * @param menu
     *            The list of strings from which the menu is constructed
     * @param cell
     *            The cell being checked in the web table        
     *           
     */
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

    public void assertCellImage(TableCell cell, String imgSrc) {
        assertEquals(1, cell.getImages().length);
        WebImage img = cell.getImages()[0];
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

    public void assertCell(TableCell cell, String contents, int colspan) {
        if (contents == null) {
            assertNull(cell);
        } else {
            assertNotNull(cell);
            assertEquals(contents, cell.getText());
            assertEquals(colspan, cell.getColSpan());
        }
    }

    public void assertCell(TableCell cell, String contents) {
        assertCell(cell, contents, 1);
    }

    public void copyFile(String origFile, String newFile) throws IOException {
        copyFile(new File(origFile), new File(newFile));
    }
    
    public void copyFile(File origFile, File newFile) throws IOException {
        assertTrue("File "+origFile+" is not readable", origFile.canRead());
        assertTrue("File "+newFile+" is not writable", !newFile.exists() || newFile.canWrite());
        
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(origFile);
            out = new FileOutputStream(newFile);
            int data;
            while ((data = in.read()) != -1) {
                out.write(data);
            }
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

}
