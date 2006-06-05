/**
 * 
 */
package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.springframework.core.io.Resource;

public class DefaultImportStatistics implements ImportStatistics {
	public void beginProcessingOps() {
	}

	public void finishProcessingOps() {
	}

	public void beginPreprocessingOps() {
	}

	public void finishPreprocessingOps() {
	}

	public void beginPreprocessing(ImportOperation oper) {
	}

	public void finishPreprocessing(ImportOperation oper) {
	}

	public void beginPersisting(ImportOperation oper) {
	}

	public void finishPersisting(ImportOperation oper) {
	}

	public void beginSendingEvents(ImportOperation oper, List events) {
	}

	public void finishSendingEvents(ImportOperation oper, List events) {
	}

	public void beginLoadingResource(Resource resource) {
	}

	public void finishLoadingResource(Resource resource) {
	}

	public void beginImporting() {
	}

	public void finishImporting() {
	}

	public void beginAuditNodes() {
	}

	public void finishAuditNodes() {
	}

	public void setDeleteCount(int deleteCount) {
	}

	public void setInsertCount(int insertCount) {
	}

	public void setUpdateCount(int updateCount) {
	}

	public void beginRelateNodes() {
		// TODO Auto-generated method stub
		
	}

	public void finishRelateNodes() {
		// TODO Auto-generated method stub
		
	}

}