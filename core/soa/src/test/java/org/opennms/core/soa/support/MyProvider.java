/**
 * 
 */
package org.opennms.core.soa.support;

import java.io.IOException;

public class MyProvider implements Hello, Goodbye {
    
    String m_name;
    int m_helloSaid = 0;
    int m_goodbyeSaid = 0;
    
    public MyProvider() {
        this("provider");
    }
    
    public MyProvider(String name) {
        m_name = name;
    }

    public void sayHello() throws IOException {
        m_helloSaid++;
    }

    public void sayGoodbye() throws IOException {
        m_goodbyeSaid++;
    }
    
    public int helloSaid() {
        return m_helloSaid;
    }
    
    public int goodbyeSaid() {
        return m_goodbyeSaid;
    }
    
    private void printf(String fmt, Object... args) {
        System.err.println(String.format("XXXXXXXXXXXXXXXXXX %s : %s --- %s", Thread.currentThread(), this, String.format(fmt, args)));
    }

    @Override
    public String toString() {
        return m_name;
    }
    
    
    
}