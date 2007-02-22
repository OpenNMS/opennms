package org.opennms.dashboard.client;

public interface Pageable {

    int getCurrentElement();
    
    void setCurrentElement(int element);

    int getPageSize();

    int getElementCount();

}
