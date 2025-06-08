/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.nodelabel;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.NodeLabel;
import org.opennms.netmgt.dao.hibernate.NodeLabelDaoImpl;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.events.NodeLabelChangedEventBuilder;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.api.Util;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.common.base.Throwables;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Changes the label of a node, throws an event signaling that change, and then
 * redirects the user to a web page displaying that node's details.
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NodeLabelChangeServlet extends HttpServlet {
    private static final long serialVersionUID = -7766362068448931124L;
    private final static Logger LOG = LoggerFactory.getLogger(NodeLabelChangeServlet.class);
    protected EventProxy proxy;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        try {
            this.proxy = Util.createEventProxy();
        } catch (Throwable e) {
            throw new ServletException("JMS Exception", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String nodeIdString = request.getParameter("node");
        String labelType = request.getParameter("labeltype");
        String userLabel = request.getParameter("userlabel");

        if (nodeIdString == null) {
            throw new MissingParameterException("node", new String[] { "node", "labeltype", "userlabel" });
        }
        if (labelType == null) {
            throw new MissingParameterException("labeltype", new String[] { "node", "labeltype", "userlabel" });
        }
        if (userLabel == null) {
            throw new MissingParameterException("userlabel", new String[] { "node", "labeltype", "userlabel" });
        }

        try {
            final int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
            final OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
            NodeLabel nodeLabel = BeanUtils.getBean("daoContext", "nodeLabel", NodeLabel.class);
            NodeLabel oldLabel = new NodeLabelDaoImpl(node.getLabel(), node.getLabelSource());
            NodeLabel newLabel = null;

            if (labelType.equals("auto")) {
                newLabel = nodeLabel.computeLabel(nodeId);
            } else if (labelType.equals("user")) {
                newLabel = new NodeLabelDaoImpl(userLabel, NodeLabelSource.USER);
            } else {
                throw new ServletException("Unexpected labeltype value: " + labelType);
            }

            final String newNodeLabel = newLabel.getLabel();
            boolean managedByProvisiond = node.getForeignSource() != null && node.getForeignId() != null;
            if (managedByProvisiond) {
                WebApplicationContext beanFactory = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
                final TransactionTemplate transactionTemplate = beanFactory.getBean(TransactionTemplate.class);
                final RequisitionAccessService requisitionService = beanFactory.getBean(RequisitionAccessService.class);
                transactionTemplate.execute(new TransactionCallback<RequisitionNode>() {
                    @Override
                    public RequisitionNode doInTransaction(TransactionStatus status) {
                        MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
                        params.putSingle("node-label", newNodeLabel);
                        requisitionService.updateNode(node.getForeignSource(), node.getForeignId(), params);
                        return requisitionService.getNode(node.getForeignSource(), node.getForeignId());
                    }
                });
            }

            this.sendLabelChangeEvent(nodeId, oldLabel, newLabel);

            if (managedByProvisiond) {
                response.sendRedirect(Util.calculateUrlBase(request, "admin/ng-requisitions/index.jsp#/requisitions/" + URLEncoder.encode(node.getForeignSource()) + "/nodes/" + URLEncoder.encode(node.getForeignId())));
            } else {
                final WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
                final TransactionTemplate transactionTemplate = context.getBean(TransactionTemplate.class);
                final NodeLabel newLabelFinal = newLabel;
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        try {
                            nodeLabel.assignLabel(nodeId, newLabelFinal);
                        } catch (SQLException e) {
                            LOG.error("Failed to change label on node with id: {} to: {}", nodeId, newLabelFinal, e);
                            throw Throwables.propagate(e);
                        }
                    }
                });
                response.sendRedirect(Util.calculateUrlBase(request, "element/node.jsp?node=" + nodeIdString));
            }
        } catch (SQLException e) {
            throw new ServletException("Database exception", e);
        } catch (Throwable e) {
            throw new ServletException("Exception sending node label change event", e);
        }
    }

    /**
     * <p>sendLabelChangeEvent</p>
     *
     * @param nodeId a int.
     * @param oldNodeLabel a {@link org.opennms.netmgt.utils.NodeLabelJDBCImpl} object.
     * @param newNodeLabel a {@link org.opennms.netmgt.utils.NodeLabelJDBCImpl} object.
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    protected void sendLabelChangeEvent(int nodeId, NodeLabel oldNodeLabel, NodeLabel newNodeLabel) throws EventProxyException {

        NodeLabelChangedEventBuilder bldr = new NodeLabelChangedEventBuilder("NodeLabelChangeServlet");
        bldr.setNodeid(nodeId);
        bldr.setHost("host");

        if (oldNodeLabel != null) {
            bldr.setOldNodeLabel(oldNodeLabel.getLabel());
            if (oldNodeLabel.getSource() != null) {
                bldr.setOldNodeLabelSource(oldNodeLabel.getSource().toString());
            }
        }

        if (newNodeLabel != null) {
            bldr.setNewNodeLabel(newNodeLabel.getLabel());
            if (newNodeLabel.getSource() != null) {
                bldr.setNewNodeLabelSource(newNodeLabel.getSource().toString());
            }
        }

        this.proxy.send(bldr.getEvent());
    }

}
