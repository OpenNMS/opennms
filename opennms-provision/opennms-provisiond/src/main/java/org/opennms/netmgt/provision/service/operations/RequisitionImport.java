package org.opennms.netmgt.provision.service.operations;

import javax.xml.bind.ValidationException;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class RequisitionImport {
    private Requisition m_requisition;
    private Throwable m_throwable;

    public Requisition getRequisition() {
        return m_requisition;
    }

    public void setRequisition(final Requisition requisition) {
        m_requisition = requisition;
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            if (m_throwable == null) {
                m_throwable = e;
            } else {
                LogUtils.debugf(this, e, "Requisition %s did not validate, but we'll ignore the exception because we've previously aborted with: %s", requisition, m_throwable);
            }
        }
    }

    public Throwable getError() {
        return m_throwable;
    }

    public void abort(final Throwable t) {
        if (m_throwable == null) {
            m_throwable = t;
        } else {
            LogUtils.warnf(this, t, "Requisition %s has already been aborted, but we received another abort message.  Ignoring.", m_requisition);
        }
    }

    public boolean isAborted() {
        if (m_throwable != null) return true;
        return false;
    }

}
