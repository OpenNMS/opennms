package org.opennms.web.element;

import java.util.Comparator;

public class InterfaceIpAddressComparator implements Comparator<Interface> {
    public int compare(Interface i1, Interface i2) {
        return i1.getIpAddress().compareTo(i2.getIpAddress());
    }

//  public boolean equals(Interface i1, Interface i2) {
//      return i1.getIpAddress().equals(i2.getIpAddress());
//  }
}
