package org.opennms.netmgt.dao.castor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.dao.AckdConfigurationDao;

public class DefaultAckdConfigurationDao extends AbstractCastorConfigDao<AckdConfiguration, AckdConfiguration> implements AckdConfigurationDao {

    public DefaultAckdConfigurationDao() {
        super(AckdConfiguration.class, "Ackd Configuration");
    }
    
    public AckdConfiguration getConfig() {
        return getContainer().getObject();
    }

    @Override
    public AckdConfiguration translateConfig(AckdConfiguration castorConfig) {
        return castorConfig;
    }

    public Boolean acknowledgmentMatch(List<String> messageText) {
        String expression = getContainer().getObject().getAckExpression();
        return matcher(messageText, expression);
    }

    public Boolean clearMatch(List<String> messageText) {
        String expression = getContainer().getObject().getClearExpression();
        return matcher(messageText, expression);
    }

    public Boolean escalationMatch(List<String> messageText) {
        String expression = getContainer().getObject().getEscalateExpression();
        return matcher(messageText, expression);
    }

    public Boolean unAcknowledgmentMatch(List<String> messageText) {
        String expression = getContainer().getObject().getUnackExpression();
        return matcher(messageText, expression);
    }

    private Boolean matcher(List<String> messageText, String expression) {
        Boolean matches = new Boolean(false);
        Pattern p;
        
        if (expression.startsWith("~")) {
            expression = (expression.startsWith("~") ? expression.substring(1) : expression); 
            p = Pattern.compile(expression);

            for (String text : messageText) {
                Matcher m = p.matcher(text);
                matches = m.matches();
                if (matches) {
                    break;
                }
            }
        } else {
            for (String text : messageText) {
                matches = expression.equalsIgnoreCase(text);
            }
        }
        return matches;
    }
    
}
