package org.opennms.web.svclayer;

import org.opennms.web.command.NodeListCommand;
import org.opennms.web.svclayer.support.NodeListModel;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface NodeListService {
    public NodeListModel createNodeList(NodeListCommand command);
}
