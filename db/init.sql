-- Create databases for each microservice.
-- PostgreSQL runs this on first startup only (when pgdata volume is empty).

CREATE DATABASE userdb;
CREATE DATABASE bookdb;
CREATE DATABASE loandb;
