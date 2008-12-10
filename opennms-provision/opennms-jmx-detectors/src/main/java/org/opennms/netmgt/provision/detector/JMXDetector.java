package org.opennms.netmgt.provision.detector;

import org.opennms.netmgt.provision.support.BasicDetector;
import org.opennms.netmgt.provision.support.ClientConversation.ResponseValidator;
import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;

public abstract class JMXDetector extends BasicDetector<ConnectionWrapper, Integer>{

    protected JMXDetector(int defaultPort, int defaultTimeout, int defaultRetries) {
        super(defaultPort, defaultTimeout, defaultRetries);
    }

    
    @Override
    protected abstract JMXClient getClient();

    
    @Override
    protected abstract void onInit();
    
    protected void expectBeanCount(ResponseValidator<Integer> bannerValidator) {
        getConversation().expectBanner(bannerValidator);
    }
    
    protected ResponseValidator<Integer> greatThan(final int count){
        return new ResponseValidator<Integer>() {

            public boolean validate(Integer response) throws Exception {
                
                return (response >= count);
            }
            
        };
    }
	
}