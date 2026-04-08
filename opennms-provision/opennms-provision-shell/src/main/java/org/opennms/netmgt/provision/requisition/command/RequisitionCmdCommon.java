package org.opennms.netmgt.provision.requisition.command;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

import java.util.Objects;

public class RequisitionCmdCommon {
    public static boolean doesFSDExist(ForeignSourceRepository deployedForeignSourceRepository, String fsName) {
        boolean fsExists = false;
        for (ForeignSource fs : deployedForeignSourceRepository.getForeignSources()) {
            if (Objects.equals(fs.getName(), fsName)) {
                fsExists = true;
                break;
            }
        }
        return fsExists;
    }

    public static boolean doesRequisitionExist(ForeignSourceRepository pendingForeignSourceRepository, String requisitionName) {
        boolean reqExists = true;
        Requisition someReq = null;
        try {
            someReq = pendingForeignSourceRepository.getRequisition(requisitionName);
        } catch (ForeignSourceRepositoryException e) {
            reqExists = false;
        }
        if (someReq == null) {
            reqExists = false;
        }
        return reqExists;
    }
}
