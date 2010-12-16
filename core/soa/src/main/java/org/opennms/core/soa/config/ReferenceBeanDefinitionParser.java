package org.opennms.core.soa.config;

import org.opennms.core.soa.support.ReferenceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * <p>ReferenceBeanDefinitionParser class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReferenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
        
    /** Constant <code>INTERFACE_ATTR="interface"</code> */
    public static final String INTERFACE_ATTR = "interface";
    public static final String FILTER_ATTR = "filter";
    
    /** {@inheritDoc} */
    protected Class<?> getBeanClass(Element element) {
        return ReferenceFactoryBean.class;
    }
    
    /** {@inheritDoc} */
    public void doParse(Element element, ParserContext context, BeanDefinitionBuilder bean){
        
        bean.addPropertyReference("serviceRegistry", Constants.SERVICE_REGISTRY_BEAN_NAME);
        
        String serviceInterface = element.getAttribute(INTERFACE_ATTR);
        if (StringUtils.hasText(serviceInterface)) {
            bean.addPropertyValue("serviceInterface", serviceInterface);
        }
        
        String filter = element.getAttribute(FILTER_ATTR);
        if (StringUtils.hasText(filter)) {
            bean.addPropertyValue("filter", filter);
        }
    }
}

