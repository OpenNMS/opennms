package org.opennms.netmgt.utils;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class RunnableConsumerThreadPoolFactoryBean implements FactoryBean, InitializingBean {
    
    private String m_name;
    private float m_lowMark = 0.0f;
    private float m_highMark = 0.0f;
    private int m_maxThreads = 0;
    
    private RunnableConsumerThreadPool m_pool;

    public Object getObject() throws Exception {
        return m_pool;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType() {
        return (m_pool == null ? RunnableConsumerThreadPoolFactoryBean.class : m_pool.getClass());
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(m_name != null, "name property must be set");

        m_pool = new RunnableConsumerThreadPool(m_name, m_lowMark, m_highMark, m_maxThreads);
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public float getLowMark() {
        return m_lowMark;
    }

    public void setLowMark(float lowMark) {
        m_lowMark = lowMark;
    }

    public float getHighMark() {
        return m_highMark;
    }

    public void setHighMark(float highMark) {
        m_highMark = highMark;
    }

    public int getMaxThreads() {
        return m_maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        m_maxThreads = maxThreads;
    }

}
