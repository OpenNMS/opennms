package org.opennms.netmgt.alarmd.drools;

import org.kie.api.runtime.rule.FactHandle;
import org.opennms.netmgt.model.AlarmAssociation;

public class AlarmAssociationAndFact {

    private AlarmAssociation alarmAssociation;
    private FactHandle fact;

    public AlarmAssociationAndFact(AlarmAssociation alarmAssociation, FactHandle fact) {
        this.alarmAssociation = alarmAssociation;
        this.fact = fact;
    }

    public AlarmAssociation getAlarmAssociation() {
        return alarmAssociation;
    }

    public void setAlarmAssociation(AlarmAssociation alarmAssociation) {
        this.alarmAssociation = alarmAssociation;
    }

    public FactHandle getFact() {
        return fact;
    }

    public void setFact(FactHandle fact) {
        this.fact = fact;
    }
}
