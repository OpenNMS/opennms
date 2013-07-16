package org.opennms.netmgt.dao.mock;

import org.springframework.dao.DataRetrievalFailureException;

public class BeanWrapperFailure extends DataRetrievalFailureException {
    private static final long serialVersionUID = -502014384653230594L;

    public BeanWrapperFailure(final String msg) {
        super(msg);
    }

    public BeanWrapperFailure(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
