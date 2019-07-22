
CREATE KEYSPACE $KEYSPACE$
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': $REPLICATION_FACTOR$};

CREATE TABLE $KEYSPACE$.kvstore (
  key varchar,
  context varchar,
  value blob,
  lastUpdated timestamp,
  PRIMARY KEY (context, key)
);
