package org.opennms.web.controller;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.modelimport.ModelImport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindException;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ImportController extends SimpleFormController {
    
    public ImportController() {
        super.setCommandName("importData");
        super.setBindOnNewForm(true);
        super.setSuccessView("debug");
    }
    
    
    
    

    @Override
    protected Object formBackingObject(HttpServletRequest arg0) throws Exception {
        Resource resource = new ClassPathResource("tec_dump.xml");
        Reader r = new InputStreamReader(resource.getInputStream());
        ModelImport importData = (ModelImport) Unmarshaller.unmarshal(ModelImport.class, r);
        r.close();
        return importData;
    }

}
