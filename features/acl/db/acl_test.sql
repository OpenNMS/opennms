SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

CREATE PROCEDURAL LANGUAGE plpgsql;

ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

CREATE TABLE authentication (
    username character varying(10) NOT NULL,
    password character varying NOT NULL,
    enabled smallint,
    id bigint NOT NULL
);

ALTER TABLE public.authentication OWNER TO postgres;

CREATE TABLE authorities (
    name character varying(50) NOT NULL,
    id integer NOT NULL,
    description character varying,
    group_id bigint
);

ALTER TABLE public.authorities OWNER TO postgres;

CREATE TABLE authorities_categories (
    authority_id integer NOT NULL,
    id integer,
    category_id integer NOT NULL
);

ALTER TABLE public.authorities_categories OWNER TO postgres;

CREATE TABLE group_members (
    username character varying(50) NOT NULL,
    group_id bigint NOT NULL,
    id bigint NOT NULL
);

ALTER TABLE public.group_members OWNER TO postgres;

CREATE TABLE groups (
    group_name character varying(50) NOT NULL,
    id bigint NOT NULL
);

ALTER TABLE public.groups OWNER TO postgres;

CREATE SEQUENCE authorities_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.authorities_categories_id_seq OWNER TO postgres;

SELECT pg_catalog.setval('authorities_categories_id_seq', 1, false);

CREATE SEQUENCE authorities_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.authorities_id_seq OWNER TO postgres;

SELECT pg_catalog.setval('authorities_id_seq', 58, true);

CREATE SEQUENCE group_members_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.group_members_id_seq OWNER TO postgres;

ALTER SEQUENCE group_members_id_seq OWNED BY group_members.id;

SELECT pg_catalog.setval('group_members_id_seq', 352, true);

CREATE SEQUENCE groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.groups_id_seq OWNER TO postgres;

SELECT pg_catalog.setval('groups_id_seq', 1, false);

CREATE SEQUENCE users_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER TABLE public.users_id_seq OWNER TO postgres;

ALTER SEQUENCE users_id_seq OWNED BY authentication.id;

SELECT pg_catalog.setval('users_id_seq', 43, true);

ALTER TABLE authentication ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);

ALTER TABLE group_members ALTER COLUMN id SET DEFAULT nextval('group_members_id_seq'::regclass);

ALTER TABLE ONLY group_members
    ADD CONSTRAINT group_members_pkey PRIMARY KEY (id);

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);

ALTER TABLE ONLY authorities
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

ALTER TABLE ONLY authentication
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);

ALTER TABLE ONLY authentication
    ADD CONSTRAINT users_username_key UNIQUE (username);

CREATE INDEX index_id ON authentication USING btree (id);

ALTER TABLE ONLY authorities
    ADD CONSTRAINT fk_authorities_groups FOREIGN KEY (group_id) REFERENCES groups(id);

ALTER TABLE ONLY group_members
    ADD CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id);

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;