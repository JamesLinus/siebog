DROP KEYSPACE IF EXISTS clientagents;

CREATE KEYSPACE clientagents 
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
  
USE clientagents;

CREATE TABLE states(
  aid text PRIMARY KEY,
  state text
);