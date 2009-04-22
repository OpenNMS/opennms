--
-- PostgreSQL database dump
--

-- Started on 2009-04-06 22:34:26 CEST

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 306 (class 2612 OID 16386)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1478 (class 1259 OID 18826)
-- Dependencies: 6
-- Name: authentication; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE authentication (
    username character varying(10) NOT NULL,
    password character varying NOT NULL,
    enabled smallint,
    id bigint NOT NULL
);


ALTER TABLE public.authentication OWNER TO postgres;

--
-- TOC entry 1479 (class 1259 OID 18832)
-- Dependencies: 1755 6
-- Name: authorities; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE authorities (
    name character varying(50) NOT NULL,
    id integer NOT NULL,
    description character varying,
    group_id bigint DEFAULT 0
);


ALTER TABLE public.authorities OWNER TO postgres;

--
-- TOC entry 1486 (class 1259 OID 19147)
-- Dependencies: 6
-- Name: authorities_categories; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE authorities_categories (
    authority_id integer NOT NULL,
    id integer NOT NULL,
    category_id integer NOT NULL
);


ALTER TABLE public.authorities_categories OWNER TO postgres;

--
-- TOC entry 1482 (class 1259 OID 18928)
-- Dependencies: 6
-- Name: group_members; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE group_members (
    username character varying(50) NOT NULL,
    group_id bigint NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE public.group_members OWNER TO postgres;

--
-- TOC entry 1481 (class 1259 OID 18922)
-- Dependencies: 6
-- Name: groups; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE groups (
    group_name character varying(50) NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE public.groups OWNER TO postgres;

--
-- TOC entry 1487 (class 1259 OID 19153)
-- Dependencies: 6
-- Name: authorities_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE authorities_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.authorities_categories_id_seq OWNER TO postgres;

--
-- TOC entry 1782 (class 0 OID 0)
-- Dependencies: 1487
-- Name: authorities_categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('authorities_categories_id_seq', 1, false);


--
-- TOC entry 1484 (class 1259 OID 19127)
-- Dependencies: 6
-- Name: authorities_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE authorities_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.authorities_id_seq OWNER TO postgres;

--
-- TOC entry 1783 (class 0 OID 0)
-- Dependencies: 1484
-- Name: authorities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('authorities_id_seq', 1, true);


--
-- TOC entry 1485 (class 1259 OID 19138)
-- Dependencies: 1482 6
-- Name: group_members_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE group_members_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.group_members_id_seq OWNER TO postgres;

--
-- TOC entry 1784 (class 0 OID 0)
-- Dependencies: 1485
-- Name: group_members_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE group_members_id_seq OWNED BY group_members.id;


--
-- TOC entry 1785 (class 0 OID 0)
-- Dependencies: 1485
-- Name: group_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('group_members_id_seq', 1, true);


--
-- TOC entry 1483 (class 1259 OID 18933)
-- Dependencies: 6
-- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE groups_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.groups_id_seq OWNER TO postgres;

--
-- TOC entry 1786 (class 0 OID 0)
-- Dependencies: 1483
-- Name: groups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('groups_id_seq', 1, true);


--
-- TOC entry 1480 (class 1259 OID 18857)
-- Dependencies: 6 1478
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE users_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO postgres;

--
-- TOC entry 1787 (class 0 OID 0)
-- Dependencies: 1480
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE users_id_seq OWNED BY authentication.id;


--
-- TOC entry 1788 (class 0 OID 0)
-- Dependencies: 1480
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('users_id_seq', 1, true);


--
-- TOC entry 1754 (class 2604 OID 18863)
-- Dependencies: 1480 1478
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE authentication ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- TOC entry 1756 (class 2604 OID 19140)
-- Dependencies: 1485 1482
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE group_members ALTER COLUMN id SET DEFAULT nextval('group_members_id_seq'::regclass);


--
-- TOC entry 1772 (class 0 OID 18826)
-- Dependencies: 1478
-- Data for Name: authentication; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO authentication (username, password, enabled, id) VALUES ('admin', 'dd94709528bb1c83d08f3088d4043f4742891f4f', 1, 1);


--
-- TOC entry 1773 (class 0 OID 18832)
-- Dependencies: 1479
-- Data for Name: authorities; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO authorities (name, id, description, group_id) VALUES ('ROLE_ADMIN', 1, 'admin', 1);


--
-- TOC entry 1776 (class 0 OID 19147)
-- Dependencies: 1486
-- Data for Name: authorities_categories; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 1775 (class 0 OID 18928)
-- Dependencies: 1482
-- Data for Name: group_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO group_members (username, group_id, id) VALUES ('admin', 1, 1);


--
-- TOC entry 1774 (class 0 OID 18922)
-- Dependencies: 1481
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO groups (group_name, id) VALUES ('GROUP_HIDDEN', 0);
INSERT INTO groups (group_name, id) VALUES ('GROUP_ADMIN', 1);


--
-- TOC entry 1769 (class 2606 OID 19158)
-- Dependencies: 1486 1486
-- Name: authorities_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY authorities_categories
    ADD CONSTRAINT authorities_categories_pkey PRIMARY KEY (id);


--
-- TOC entry 1767 (class 2606 OID 19146)
-- Dependencies: 1482 1482
-- Name: group_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY group_members
    ADD CONSTRAINT group_members_pkey PRIMARY KEY (id);


--
-- TOC entry 1765 (class 2606 OID 18932)
-- Dependencies: 1481 1481
-- Name: groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- TOC entry 1763 (class 2606 OID 18878)
-- Dependencies: 1479 1479
-- Name: roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- TOC entry 1759 (class 2606 OID 18888)
-- Dependencies: 1478 1478
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY authentication
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);


--
-- TOC entry 1761 (class 2606 OID 18890)
-- Dependencies: 1478 1478
-- Name: users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY authentication
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 1757 (class 1259 OID 18891)
-- Dependencies: 1478
-- Name: index_id; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX index_id ON authentication USING btree (id);


--
-- TOC entry 1770 (class 2606 OID 18961)
-- Dependencies: 1764 1481 1479
-- Name: fk_authorities_groups; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT fk_authorities_groups FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- TOC entry 1771 (class 2606 OID 18946)
-- Dependencies: 1764 1481 1482
-- Name: fk_group_members_group; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY group_members
    ADD CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- TOC entry 1781 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2009-04-06 22:34:26 CEST

--
-- PostgreSQL database dump complete
--

