-- Runs once, on first initialization of the postgres data volume.
-- Matches tools/local_development/postgres/init.sql: OpenNMS connects as the
-- unprivileged 'opennms' role, while the installer uses 'postgres'.
CREATE USER opennms WITH PASSWORD 'opennms';
GRANT ALL PRIVILEGES ON DATABASE opennms TO opennms;
GRANT CREATE ON SCHEMA public TO opennms;
