
CREATE KEYSPACE $KEYSPACE$
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': $REPLICATION_FACTOR$};

CREATE TABLE $KEYSPACE$.kvstore (
  key varchar PRIMARY KEY,
  value blob,
  lastUpdated timestamp
);
