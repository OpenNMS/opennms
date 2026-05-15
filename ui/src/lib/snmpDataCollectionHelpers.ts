/**
 * Helpers for the SNMP Data Collection admin pages.
 *
 * The single source of truth for "is this row managed by a plugin?" lives on
 * the backend (SnmpDataCollectionSyncToDb.PLUGIN_UPLOADED_BY). Plugin-managed
 * sources are read-only in the UI for content edits and deletes, but admins
 * can still toggle their enabled flag.
 */

/**
 * Marker value the backend writes to {@code SnmpCollectionSource.uploaded_by}
 * for rows owned by the plugin sync. Must stay in sync with the constant of
 * the same name in {@code SnmpDataCollectionSyncToDb.java}.
 */
export const PLUGIN_UPLOADED_BY = 'opennms-plugins'

/**
 * Whether a source row was contributed by a plugin (and is therefore
 * read-only for content edits/deletes in the UI).
 *
 * Accepts a partial source object so it can be called against the various
 * table-row shapes the UI uses without coercion.
 */
export const isPluginSourced = (source: { uploadedBy?: string | null } | null | undefined): boolean => {
  return !!source && source.uploadedBy === PLUGIN_UPLOADED_BY
}
