package org.opennms.web.element;

import java.util.Comparator;

public class ServiceNameComparator implements Comparator<Service> {
    public int compare(Service s1, Service s2) {
        return s1.getServiceName().compareTo(s2.getServiceName());
    }

//  public boolean equals(Service s1, Service s2) {
//      return s1.getServiceName().equals(s2.getServiceName());
//  }        
}
