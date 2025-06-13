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
package org.opennms.web.controller.admin.group;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.spring.BeanUtils;
import org.opennms.web.group.WebGroup;
import org.opennms.web.group.WebGroupRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>SaveGroupController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SaveGroupController extends AbstractController implements InitializingBean {
    
    @Autowired
    WebGroupRepository m_groupRepository;
    
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HttpSession user = request.getSession(false);

        if (user != null) {
            WebGroup newGroup = (WebGroup) user.getAttribute("group.modifyGroup.jsp");
            m_groupRepository.saveGroup(newGroup);
        }


        return new ModelAndView("redirect:/admin/userGroupView/groups/list.jsp");
    }

}
