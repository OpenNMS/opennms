/*
 * Created on 27-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.inventory;

import java.util.*;
import java.io.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * Class Comparator compare 2 object
 * org.opennms.netmgt.config.inventory.parser.Inventory and build two html
 * frames to insert in the page conf/viewcmpinventory.jsp
 * 
 * @author maurizio
 */
public class Comparator {
    private org.opennms.netmgt.config.inventory.parser.Inventory firstInventory;

    private org.opennms.netmgt.config.inventory.parser.Inventory secondInventory;

    private List firstInventoryItemList = new ArrayList();

    private List secondInventoryItemList = new ArrayList();

    /**
     * Compares old and new inventory
     * 
     * @return
     * @throws ValidationException
     * @throws MarshalException
     * @throws UnsupportedOperationException
     */
    public void compare() throws UnsupportedOperationException {

        InventoryItem invIt1 = new InventoryItem(firstInventory.getName());
        InventoryItem invIt2 = new InventoryItem(secondInventory.getName());
        if (!invIt1.equals(invIt2)) {
            throw new UnsupportedOperationException(
                    "Unable to compare different inventories.");
        }
        firstInventoryItemList.add(invIt1);
        secondInventoryItemList.add(invIt2);
        visit(firstInventory, secondInventory);
    }

    /**
     * @param inventory
     */
    public void setFirstInventory(
            org.opennms.netmgt.config.inventory.parser.Inventory inventory) {
        firstInventory = inventory;
    }

    /**
     * @param inventory
     */
    public void setSecondInventory(
            org.opennms.netmgt.config.inventory.parser.Inventory inventory) {
        secondInventory = inventory;
    }

    /**
     * @param inventory
     */
    public void setFirstInventory(String firstInvPath)
            throws FileNotFoundException, ValidationException, MarshalException {
        firstInventory = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller
                .unmarshal(
                        org.opennms.netmgt.config.inventory.parser.Inventory.class,
                        new FileReader(firstInvPath));

    }

    /**
     * @param inventory
     */
    public void setSecondInventory(String secondInvPath)
            throws FileNotFoundException, ValidationException, MarshalException {
        secondInventory = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller
                .unmarshal(
                        org.opennms.netmgt.config.inventory.parser.Inventory.class,
                        new FileReader(secondInvPath));

    }

    private void visit(
            org.opennms.netmgt.config.inventory.parser.Inventory firstInventory,
            org.opennms.netmgt.config.inventory.parser.Inventory secondInventory) {

        Enumeration enumFirst = firstInventory.enumerateItem();
        while (enumFirst.hasMoreElements()) {
            Item firstIt = (Item) enumFirst.nextElement();
            InventoryItem invIt1 = new InventoryItem(firstIt,
                    InventoryItem.EQUAL_STATUS, 1);
            Enumeration enumSecond = secondInventory.enumerateItem();
            boolean foundFirst = false;
            while (enumSecond.hasMoreElements()) {
                Item secIt = (Item) enumSecond.nextElement();
                InventoryItem invIt2 = new InventoryItem(secIt,
                        InventoryItem.EQUAL_STATUS, 1);
                if (invIt1.getName().equals(invIt2.getName())) {
                    foundFirst = true;
                    visitAndCompare(firstIt, secIt, 0);
                }
            }
            if (!foundFirst) {
                int rows = visitSubTree(firstIt, 0, firstInventoryItemList);
                //add a blank InventoryItem to second list
                secondInventoryItemList.add(new InventoryItem(
                        InventoryItem.REMOVED_STATUS, rows, 1));
            }
        }

        Enumeration enumSecond = secondInventory.enumerateItem();
        while (enumSecond.hasMoreElements()) {
            Item secondIt = (Item) enumSecond.nextElement();
            InventoryItem invIt2 = new InventoryItem(secondIt,
                    InventoryItem.EQUAL_STATUS, 1);
            enumFirst = firstInventory.enumerateItem();
            boolean foundSecond = false;
            while (enumFirst.hasMoreElements()) {
                Item firstIt = (Item) enumFirst.nextElement();
                InventoryItem invIt1 = new InventoryItem(firstIt,
                        InventoryItem.EQUAL_STATUS, 1);
                if (invIt2.getName().equals(invIt1.getName())) {
                    foundSecond = true;
                }
            }
            if (!foundSecond) {
                int rows = visitSubTree(secondIt, 0, secondInventoryItemList);
                //add a blank InventoryItem to first list
                firstInventoryItemList.add(new InventoryItem(
                        InventoryItem.REMOVED_STATUS, rows, 1));
            }
        }
    }

    private boolean isLeaf(Item item) {
        if (item.getItemCount() <= 0)
            return true;
        return false;
    }

    private void visitAndCompare(Item firstItem, Item secondItem, int numColumns) {
        numColumns++;
        if (isLeaf(firstItem) || isLeaf(secondItem)) {
            if (isLeaf(firstItem)) {
                if (isLeaf(secondItem)) {
                    InventoryItem invIt1 = new InventoryItem(firstItem,InventoryItem.EQUAL_STATUS, numColumns);
                    InventoryItem invIt2 = new InventoryItem(secondItem,InventoryItem.EQUAL_STATUS, numColumns);
                    List listToAddEmptyItem = null;
                    InventoryItem invItTmp = null;
                    if (!invIt1.equals(invIt2)) {
                        int n1, n2;
                        n1 = invIt1.getNumRows();
                        n2 = invIt2.getNumRows();
                        if (n1 < n2) {
                            invItTmp = new InventoryItem(
                                    InventoryItem.EMPTY_STATUS, n2 - n1,numColumns);
                            listToAddEmptyItem = firstInventoryItemList;
                        } else if (n2 < n1) {
                            invItTmp = new InventoryItem(InventoryItem.EMPTY_STATUS, n1 - n2,numColumns);
                            listToAddEmptyItem = secondInventoryItemList;
                        }
                        invIt1.setStatus(InventoryItem.CHANGED_STATUS);
                        invIt2.setStatus(InventoryItem.CHANGED_STATUS);
                    }
                    firstInventoryItemList.add(invIt1);
                    secondInventoryItemList.add(invIt2);
                    // listToAddEmptyItem != null emplicit with invItTmp != null
                    if (invItTmp != null)
                        listToAddEmptyItem.add(invItTmp);
                    return;
                }
                InventoryItem invIt1 = new InventoryItem(firstItem,
                        InventoryItem.ADDED_STATUS, numColumns);
                firstInventoryItemList.add(invIt1);
                int numRows = invIt1.getNumRows();
                InventoryItem invIt2 = new InventoryItem(
                        InventoryItem.REMOVED_STATUS, numRows, numColumns);
                secondInventoryItemList.add(invIt2);
                int countRows = visitSubTree(secondItem, numColumns,
                        secondInventoryItemList);
                invIt1 = new InventoryItem(InventoryItem.REMOVED_STATUS,
                        countRows, numColumns);
                firstInventoryItemList.add(invIt1);
            } else {
                InventoryItem invIt2 = new InventoryItem(secondItem,
                        InventoryItem.ADDED_STATUS, numColumns);
                secondInventoryItemList.add(invIt2);
                int numRows = invIt2.getNumRows();
                InventoryItem invIt1 = new InventoryItem(
                        InventoryItem.REMOVED_STATUS, numRows, numColumns);
                firstInventoryItemList.add(invIt1);
                int countRows = visitSubTree(firstItem, numColumns,
                        firstInventoryItemList);
                invIt2 = new InventoryItem(InventoryItem.REMOVED_STATUS,
                        countRows, numColumns);
                secondInventoryItemList.add(invIt2);
            }
        } else {
            InventoryItem invIt1 = new InventoryItem(firstItem.getName());
            invIt1.setNumColumn(numColumns);
            firstInventoryItemList.add(invIt1);

            InventoryItem invIt2 = new InventoryItem(secondItem.getName());
            invIt2.setNumColumn(numColumns);
            secondInventoryItemList.add(invIt2);

            Enumeration enumFirst = firstItem.enumerateItem();

            while (enumFirst.hasMoreElements()) {
                Item firstIt = (Item) enumFirst.nextElement();
                String firstItName = firstIt.getName();
                Enumeration enumSecond = secondItem.enumerateItem();
                boolean foundFirst = false;
                while (enumSecond.hasMoreElements()) {
                    Item secIt = (Item) enumSecond.nextElement();
                    if (secIt.getName().equals(firstItName)) {
                        foundFirst = true;
                        visitAndCompare(firstIt, secIt, numColumns);
                    }
                }
                if (!foundFirst) {
                    int numRows = visitSubTree(firstIt, numColumns,
                            firstInventoryItemList);
                    InventoryItem invIt = new InventoryItem(
                            InventoryItem.REMOVED_STATUS, numRows, numColumns);
                    secondInventoryItemList.add(invIt);
                }
            }

            Enumeration enumSecond = secondItem.enumerateItem();
            while (enumSecond.hasMoreElements()) {
                Item secondIt = (Item) enumSecond.nextElement();
                String secondItName = secondIt.getName();
                enumFirst = firstItem.enumerateItem();
                boolean foundSecond = false;
                while (enumFirst.hasMoreElements()) {
                    Item firstIt = (Item) enumFirst.nextElement();
                    if (firstIt.getName().equals(secondItName)) {
                        foundSecond = true;
                    }
                }
                if (!foundSecond) {
                    int numRows = visitSubTree(secondIt, numColumns,
                            secondInventoryItemList);
                    InventoryItem invIt = new InventoryItem(
                            InventoryItem.REMOVED_STATUS, numRows, numColumns);
                    firstInventoryItemList.add(invIt);
                }
            }
        }
    }

    private int visitSubTree(Item currItem, int numColumns,
            List inventoryItemList) {
        numColumns++;

        InventoryItem invIt = new InventoryItem(currItem,
                InventoryItem.ADDED_STATUS, numColumns);
        inventoryItemList.add(invIt);

        if (isLeaf(currItem)) {
            return invIt.getNumRows();
        }

        Enumeration enumItem = currItem.enumerateItem();
        int retNumRows = 1;
        while (enumItem.hasMoreElements()) {
            retNumRows += visitSubTree((Item) enumItem.nextElement(),
                    numColumns, inventoryItemList);
        }
        return retNumRows;
    }

    public static void main(String[] args) throws Exception {
        Comparator c = new Comparator();
        System.out.println("ciao");
        c.setSecondInventory("C:\\HW_1.xml");
        c.setFirstInventory("C:\\HW_2.xml");
        c.compare();
        List first = c.getFirstInventoryItemList();
        List second = c.getSecondInventoryItemList();
        InventoryItem currentItem;
        String str = "";
        int count = 0;
        for (int i = 0, n = first.size(); i < n; i++) {
            currentItem = (InventoryItem) first.get(i);
            count += currentItem.getNumRows();
            System.out.println(currentItem.getNumRows());
            str += "<div style='padding-left: "
                    + (10 * currentItem.getNumColumn()) + "'>";
            str += currentItem.getName()
                    + ((currentItem.getDataitem() != null) ? "<br>"
                            + currentItem.getDataitem() : "");
            str += "</div>";
        }
        System.out.println("prima:" + count);
        count = 0;
        for (int i = 0, n = second.size(); i < n; i++) {
            currentItem = (InventoryItem) second.get(i);
            count += currentItem.getNumRows();
            System.out.println(currentItem.getNumRows());
            str += "<div style='padding-left: "
                    + (10 * currentItem.getNumColumn()) + "'>";
            str += currentItem.getName()
                    + ((currentItem.getDataitem() != null) ? "<br>"
                            + currentItem.getDataitem() : "");
            str += "</div>";
        }
        System.out.println("seconda:" + count);
        FileWriter fw = new FileWriter("c:\\ciao.html");
        fw.write(str);
        fw.close();
    }

    /**
     * @return Returns the firstInventoryItemList.
     */
    public List getFirstInventoryItemList() {
        return firstInventoryItemList;
    }

    /**
     * @return Returns the secondInventoryItemList.
     */
    public List getSecondInventoryItemList() {
        return secondInventoryItemList;
    }
}