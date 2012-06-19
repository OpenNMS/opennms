package org.opennms.web.alarm;

import org.apache.commons.lang.builder.ToStringBuilder;

class ReductionKeyMemo extends Memo {
    private String reductionKey;

    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
