package org.opennms.netmgt.dao.support;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@Conditional(ConditionalRrdDaoContext.Condition.class)
@ImportResource("/META-INF/opennms/component-dao-ext-rrd.xml")
public class ConditionalRrdDaoContext {
    static class Condition implements ConfigurationCondition {
         @Override
         public ConfigurationPhase getConfigurationPhase() {
             return ConfigurationPhase.PARSE_CONFIGURATION;
         }

         @Override
         public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
             return !"org.opennms.netmgt.rrd.newts.NewtsRrdStrategy".equals(System.getProperty("org.opennms.rrd.strategyClass"));
         }
    }
}
