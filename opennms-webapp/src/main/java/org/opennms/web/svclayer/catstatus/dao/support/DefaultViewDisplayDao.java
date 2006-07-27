package org.opennms.web.svclayer.catstatus.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.opennms.web.svclayer.catstatus.dao.ViewDisplayDao;
import org.springframework.dao.DataRetrievalFailureException;


public class DefaultViewDisplayDao implements ViewDisplayDao {
	
	public DefaultViewDisplayDao() {
		try {
			ViewsDisplayFactory.init();
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate viewsDisplaly file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load viewsDisplay file", e);
		}
	}
	
	public View getView() {
		try {
			return ViewsDisplayFactory.getInstance().getView("WebConsoleView");
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Syntax error in viewsDisplay file", e);
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate viewsDisplaly file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load viewsDisplay file", e);
		}
		
	}

}
