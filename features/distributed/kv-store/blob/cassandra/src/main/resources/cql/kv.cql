
CREATE KEYSPACE IF NOT EXISTS $KEYSPACE$
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': $REPLICATION_FACTOR$};

CREATE TABLE IF NOT EXISTS $KEYSPACE$.kvstore_blob (
  key varchar,
  context varchar,
  value blob,
  lastUpdated timestamp,
  PRIMARY KEY ((context, key))
);

CREATE INDEX IF NOT EXISTS key_idx ON $KEYSPACE$.kvstore_blob (key);
CREATE INDEX IF NOT EXISTS context_idx ON $KEYSPACE$.kvstore_blob (context);