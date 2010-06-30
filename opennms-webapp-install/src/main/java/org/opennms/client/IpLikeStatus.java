package org.opennms.client;

/**
 * Contains possible states of the <code>iplike</code> database function.
 *
 * @author ranger
 * @version $Id: $
 */
public enum IpLikeStatus {
    /**
     * <code>iplike</code> could not be found in a usable state in the database.
     */ 
    MISSING,
    /**
     * <code>iplike</code> was found in the database but could not be called successfully.
     */ 
    UNUSABLE,
    /**
     * <code>iplike</code> was usable but the language could not be determined.
     */ 
    UNKNOWN_LANGUAGE,
    /**
     * The native C version of <code>iplike</code> was found in the database.
     */ 
    C, 
    /**
     * A SQL version of <code>iplike</code> was found in the database.
     */ 
    SQL,
    /**
     * The PL/pgSQL version of <code>iplike</code> was found in the database.
     */ 
    PLPGSQL
}
