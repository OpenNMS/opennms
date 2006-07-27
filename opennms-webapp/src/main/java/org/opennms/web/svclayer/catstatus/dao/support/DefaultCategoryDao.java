package org.opennms.web.svclayer.catstatus.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.web.svclayer.catstatus.dao.CategoryDao;
import org.springframework.dao.DataRetrievalFailureException;

public class DefaultCategoryDao implements CategoryDao {
	
	public DefaultCategoryDao() {
		try {
			CategoryFactory.init();
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in categories file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Validation error in categories file", e);
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate categories file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load categories file", e);
		}
	}

	public Category getCategoryByLabel(String label) {
		return CategoryFactory.getInstance().getCategory(label);
	}


	
	
	
	
	
}
