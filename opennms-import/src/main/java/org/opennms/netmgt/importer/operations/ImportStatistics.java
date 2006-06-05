package org.opennms.netmgt.importer.operations;

import java.util.List;

import org.springframework.core.io.Resource;

public interface ImportStatistics {

	void beginProcessingOps();

	void finishProcessingOps();

	void beginPreprocessingOps();

	void finishPreprocessingOps();

	void beginPreprocessing(ImportOperation oper);

	void finishPreprocessing(ImportOperation oper);

	void beginPersisting(ImportOperation oper);

	void finishPersisting(ImportOperation oper);

	void beginSendingEvents(ImportOperation oper, List events);

	void finishSendingEvents(ImportOperation oper, List events);

	void beginLoadingResource(Resource resource);

	void finishLoadingResource(Resource resource);

	void beginImporting();

	void finishImporting();

	void beginAuditNodes();

	void finishAuditNodes();

	void setDeleteCount(int deleteCount);

	void setInsertCount(int insertCount);

	void setUpdateCount(int updateCount);

	void beginRelateNodes();

	void finishRelateNodes();

}
