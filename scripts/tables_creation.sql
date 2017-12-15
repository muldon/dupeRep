--execute this section if tables do not yet exist. These scripts are common to duppredictor and dupe.

CREATE SEQUENCE recallrate_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


--DROP SEQUENCE experiment_id;

CREATE SEQUENCE experiment_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

-- DROP TABLE experiment;


CREATE TABLE experiment
(
  id integer NOT NULL,
  tag character varying(50),
  numberoftestedquestions integer,
  date character varying(30),
  ttweight numeric(3,2),
  ccweight numeric(3,2),
  bbweight numeric(3,2),
  btweight numeric(3,2),
  topictopicweight numeric(3,2),
  tbweight numeric(3,2),
  aaweight numeric(3,2),
  bm25k numeric(3,2),
  bm25b numeric(3,2),
  observacao character varying(500),
  app character varying(100),
  base character varying(200),
  maxresultsize integer,
  lote integer,
  estimateWeights boolean,
  duration character varying(50),
  trm character varying(30),
  tagtagweight numeric(3,2),
  CONSTRAINT experiment_pk PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE experiment
  OWNER TO postgres;




-- DROP TABLE recallrate;
CREATE TABLE recallrate
(
  id integer NOT NULL,
  origem character varying(50),
  hits50000 integer,
  hits10000 integer,
  hits1000 integer,
  hits100 integer,
  hits50 integer,
  hits20 integer,
  hits10 integer,
  hits5 integer,
  hits1 integer,
  recallrate_50000 numeric(5,2),
  recallrate_10000 numeric(5,2),
  recallrate_1000 numeric(5,2),
  recallrate_100 numeric(5,2),
  recallrate_50 numeric(5,2),
  recallrate_20 numeric(5,2),
  recallrate_10 numeric(5,2),
  recallrate_5 numeric(5,2),
  recallrate_1 numeric(5,2),
  experiment_id integer,
  CONSTRAINT recall_pk PRIMARY KEY (id),
  CONSTRAINT experiment_fk FOREIGN KEY (experiment_id)
      REFERENCES experiment (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE recallrate
  OWNER TO postgres;



--end


--execute this to run dupe. These scripts are specific.

CREATE SEQUENCE feature_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


CREATE SEQUENCE pair_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;



--drop table pair;
 CREATE TABLE pair (
    id int PRIMARY KEY NOT NULL,
    question1 int NOT NULL,
    question2 int NOT NULL,
    duplicated boolean NOT NULL,
    maintag text DEFAULT NULL   
);

--DROP TABLE feature;

CREATE TABLE feature
(
  id integer NOT NULL,	
  pairid integer NOT NULL,
  type character varying(30) NOT NULL,
  cosine numeric(5,4) DEFAULT 0,
  termoverlap numeric(5,4) DEFAULT 0,
  entityoverlap numeric(5,4) DEFAULT 0,
  typeoverlap numeric(5,4) DEFAULT 0,
  wordnet numeric(5,4) DEFAULT 0,
  CONSTRAINT feature_pk PRIMARY KEY (id),
  CONSTRAINT pair_fk FOREIGN KEY (pairid)
      REFERENCES pair (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE feature
  OWNER TO postgres;


ALTER TABLE posts ADD COLUMN tagssyn text;
ALTER TABLE posts ADD COLUMN code text;

