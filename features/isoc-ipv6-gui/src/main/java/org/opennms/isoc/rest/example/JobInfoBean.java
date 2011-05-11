package org.opennms.isoc.rest.example;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name= "job")
public class JobInfoBean {

    public String m_name;
    public String m_status;
    public int m_pages;
    
    public JobInfoBean() {};
    
    public JobInfoBean(String name, String status, int pages) {
        this.m_name = name;
        this.m_status = status;
        this.m_pages = pages;
    }
    
}
