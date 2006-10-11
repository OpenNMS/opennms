package org.opennms.netmgt.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class DataSourceFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    public Object getObject() throws Exception {
        return DataSourceFactory.getDataSource();
    }

    public Class getObjectType() {
        return DataSourceFactory.getDataSource().getClass();
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        DataSourceFactory.init();
    }

    public void destroy() throws Exception {
        DataSourceFactory.close();
    }

}
