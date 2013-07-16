/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.api.OnmsDao;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A UpsertTemplate for handling the update if it exists/insert if it doesn't case in the midst of 
 * concurrent database updates.  The pattern for solving this is known as an Upsert.. update or insert.
 * 
 * Example Scenario:  During a node scan an interface is found on a node and various information about
 * this interface is gathered.  This information needs to be persisted to the database.  There are two 
 * cases:
 * 
 * 1.  The interface is not yet in the database so it needs to be inserted
 * 2.  The interface is already in the database so it needs to be updated
 * 
 * The naive implementation of this is something like the following (this is just pseudo code)
 *
 *   // find an interface in the db matching our scanned info
 *   SnmpInterface dbIf = m_dao.query(scannedIf);
 *   if (dbIf != null) {
 *      // found an if in the db... updated with found info
 *      retrun update(dbIf, scannedIf);
 *   } else {
 *      // no if in the db.. insert the one we found
 *      return insert(scannedIf);
 *   }
 * 
 * Problem:  The naive implementation above has the problem that it fails in the midst of concurrency.
 * Consider the following scenario where two different provisioning threads decide to update/insert 
 * the same interface that does not yet exist in the db:
 * 
 * 1 Thread 1 attempts to find the if and finds it is not there
 * 2 Thread 2 attempts to find the if and finds it is not there
 * 3 Thread 1 inserts the if into the database
 * 4 Thread 1 completes and moves onto further work
 * 5 Thread 2 attempts to insert the if into the database and a duplication exception is thrown
 * 6 All work done in Thread 2's transactions is rolled back.
 * 
 * Most people assume the 'transactions' will handle this case but they do not.  The reason for this is
 * because transactions lock the information that is found to ensure that this information is not changed
 * by others.  However when you perform a query that returns nothing there is nothing to lock.  So this case
 * is not protected by the transaction.
 * 
 * Solution:  To handle this we must execute the insert in a 'sub transaction' and retry in the event of failure:  The
 * basic loop looks something like the following pseudo code:
 * 
 * while(true) {
 *   SnmpInterface dbIf = m_dao.query(scannedIf);
 *   if (dbIf != null) {
 *      return update(dbIf, scannedIf);
 *   } else {
 *     try {
 *       // start a new sub transaction here that rolls back on exception
 *       return insert(scannedIf)
 *     } catch(Exception e) {
 *        // log failure and let the loop retry
 *     }    
 *   }    
 * }
 * 
 * This is simplified loop because it does not show all of the code to start transactions and such nor to it show
 * the real.
 * 
 * As far as code goes this solution has a great deal of boiler plate code.  This class contains this boilerplate
 * code using the Template Method pattern and provides abstract methods for filling running the query and for doing the
 * insert and/or the update.  To use this class to do the above would look something like this:
 *
 * final SnmpInterface scannedIf = ...;
 * return UpsertTemplate<SnmpInterface>(transactionManager) {
 *    @Override
 *    public SnmpInterface query() {
 *       return m_dao.query(scannedIf);
 *    }
 *    @Override
 *    public SnmpInterface doUpdate(SnmpInterface dbIf) {
 *       return update(dbIf, scannedIf);
 *    }
 *    public SnmpInterface doInsert() {
 *       return insert(scannedIf);
 *    }
 *    
 * }.execute();
 * 
 * The above will handle all of the exceptions that can occur in the face of concurrent inserts and will properly either
 * insert or update the interface.
 * 
 *
 * @author brozow
 */
public abstract class UpsertTemplate<T, D extends OnmsDao<T, ?>> {
    protected final PlatformTransactionManager m_transactionManager;
    protected final D m_dao;
    

    /**
     * Create an UpsertTemplate using the PlatformTransactionManager for creating
     * transactions.  This will retry a failed insert no more than two times.
     */
    public UpsertTemplate(PlatformTransactionManager transactionManager, D dao) {
        m_transactionManager = transactionManager;
        m_dao = dao;
    }

    /**
     * After creating the UpsertTemplate call this method to attempt the upsert.
     */
    public T execute() {
        TransactionTemplate template = new TransactionTemplate(m_transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return template.execute(new TransactionCallback<T>() {

            @Override
            public T doInTransaction(TransactionStatus status) {
                return doUpsert();
            }
        });
        
    }
    
    /**
     * Called from upsert after it creates a transaction.
     */
    private T doUpsert() {
        T dbObj = query();
        if (dbObj != null) {
            // if we found the object then update and return
            return update(dbObj);
        }

        // lock the table since we are about to insert and don't want it inserted 
        m_dao.lock();

        // make sure it wasn't inserted while we waited for the lock
        dbObj = query();
        if (dbObj != null) {
            // if was!! so update and return
            return update(dbObj);
        }
        
        // now it is save to insert it
        return insert();
    }

    /**
     * Called by doUpsert to update the object.  It delegates to doUpdate so the
     * doUpdate and doInsert method have the same from.
     */
    private T update(T dbObj) {
        return doUpdate(dbObj);
    }
    
    /**
     * Called by doUpsert to insert the object.  This method starts a new transaction
     * and executes the doInsert method in it.  The new transaction is rolled back when 
     * an exception is thrown.  The exception is handled in doUpsert
     */
    private T insert() {
        return doInsert();
    }

    /**
     * Override this method to execute the query that is used to determine if there is an
     * existing object in the database
     */
    protected abstract T query();
    
    /**
     * Override this method to update the object in the database.  The object found in the query
     * is passed into this method so it can be used to do the updating. 
     */
    protected abstract T doUpdate(T dbObj);
    
    /**
     * Override this method to insert a new object into the database. This method will be called
     * when the query method returns null.  A DataIntegrityViolationException should be thrown if 
     * the insert has already occurred. (This is the normal exception thrown when to objects with the 
     * same id are inserted).
     */
    protected abstract T doInsert();

}