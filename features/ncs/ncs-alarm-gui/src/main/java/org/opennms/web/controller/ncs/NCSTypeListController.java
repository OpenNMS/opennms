package org.opennms.web.controller.ncs;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.ncs.NCSBuilder;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponent.DependencyRequirements;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("ncs/ncs-type.htm")
public class NCSTypeListController {
    
    @Autowired
    NCSComponentRepository m_componentDao;
    
    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        String foreignSource = request.getParameter("foreignSource");
        String foreignId = request.getParameter("foreignId");
        
        NCSComponent component = null;
        String treeView = "<br/><p>No Components To View, Please check the type, foreign source and foreign id are correct</p>";
        if(!type.equals("null") && !foreignSource.equals("null") && !foreignId.equals("null")) {
            component = m_componentDao.findByTypeAndForeignIdentity(type, foreignSource, foreignId);
            treeView = "<ul class=\"TreeView\" id=\"TreeView\">\n" + getComponentHTML(component) + "</ul>";
        }
        
        
        ModelAndView modelAndView = new ModelAndView("ncs/ncs-type");
        modelAndView.addObject("treeView", treeView);
        return modelAndView;
    }
    
    private String getComponentHTML(NCSComponent component) {
        StringBuffer buffer = new StringBuffer();
        
        if(component != null) {
            Set<NCSComponent> subcomponents = component.getSubcomponents();
            if(subcomponents.size() > 0) {
                buffer.append("<li class=\"Expanded\">");
                buffer.append(component.getName());
                
                buffer.append("<ul>\n");
                for(NCSComponent c : subcomponents) {
                    buffer.append(getComponentHTML(c));
                }
                buffer.append("</ul>");
            }else {
                buffer.append("<li>");
                buffer.append(component.getName());
            }
            
            buffer.append("</li>\n");
        }
        return buffer.toString();
        
    }
    
    //Used for testing the controller view
    private NCSComponent getTestComponent() {
        return new NCSBuilder("Service", "NA-Service", "123")
        .setName("CokeP2P")
        .pushComponent("ServiceElement", "NA-ServiceElement", "8765")
            .setName("PE1:SE1")
            .setNodeIdentity("space", "1111-PE1")
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnIf")
                .setName("jnxVpnIf")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
                .setAttribute("jnxVpnIfVpnType", "5")
                .setAttribute("jnxVpnIfVpnName", "ge-1/0/2.50")
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:link")
                    .setName("link")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
                    .setAttribute("linkName", "ge-1/0/2")
                .popComponent()
            .popComponent()
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:jnxVpnPw-vcid(50)")
                .setName("jnxVpnPw-vcid(50)")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
                .setAttribute("jnxVpnPwVpnType", "5")
                .setAttribute("jnxVpnPwVpnName", "ge-1/0/2.50")
                .setDependenciesRequired(DependencyRequirements.ANY)
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspA-PE1-PE2")
                    .setName("lspA-PE1-PE2")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspA-PE1-PE2")
                .popComponent()
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "8765:lspB-PE1-PE2")
                    .setName("lspB-PE1-PE2")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspB-PE1-PE2")
                .popComponent()
            .popComponent()
        .popComponent()
        .pushComponent("ServiceElement", "NA-ServiceElement", "9876")
            .setName("PE2:SE1")
            .setNodeIdentity("space", "2222-PE2")
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnIf")
                .setName("jnxVpnIf")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfDown")
                .setAttribute("jnxVpnIfVpnType", "5")
                .setAttribute("jnxVpnIfVpnName", "ge-3/1/4.50")
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:link")
                    .setName("link")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/linkUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/linkDown")
                    .setAttribute("linkName", "ge-3/1/4")
                .popComponent()
            .popComponent()
            .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:jnxVpnPw-vcid(50)")
                .setName("jnxVpnPw-vcid(50)")
                .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwUp")
                .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnPwDown")
                .setAttribute("jnxVpnPwVpnType", "5")
                .setAttribute("jnxVpnPwVpnName", "ge-3/1/4.50")
                .setDependenciesRequired(DependencyRequirements.ANY)
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspA-PE2-PE1")
                    .setName("lspA-PE2-PE1")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspA-PE2-PE1")
                .popComponent()
                .pushComponent("ServiceElementComponent", "NA-SvcElemComp", "9876:lspB-PE2-PE1")
                    .setName("lspB-PE2-PE1")
                    .setUpEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathUp")
                    .setDownEventUei("uei.opennms.org/vendor/Juniper/traps/mplsLspPathDown")
                    .setAttribute("mplsLspName", "lspB-PE2-PE1")
                .popComponent()
            .popComponent()
        .popComponent()
        .get();
    }
}
