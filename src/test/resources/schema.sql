CREATE TABLE essaim (
    id bigint NOT NULL,
    actif boolean NOT NULL,
    commentaire character varying(255),
    date_acquisition date,
    nom character varying(255) NOT NULL,
    reine_date_naissance date,
    reine_marquee boolean NOT NULL,
    souche_id bigint,
    agressivite integer,
    proprete integer
);

CREATE TABLE evenement (
    id bigint NOT NULL,
    commentaire character varying(255),
    date timestamp without time zone,
    type integer,
    essaim_id bigint,
    hausse_id bigint,
    ruche_id bigint,
    rucher_id bigint,
    valeur character varying(64)
);

CREATE TABLE hausse (
    id bigint NOT NULL,
    active boolean NOT NULL,
    commentaire character varying(255),
    date_acquisition date,
    nb_cadres integer,
    nb_cadres_max integer,
    nom character varying(255) NOT NULL,
    ordre_sur_ruche integer,
    poids_vide integer,
    ruche_id bigint
);

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE personne (
    id bigint NOT NULL,
    active boolean NOT NULL,
    adresse character varying(255) NOT NULL,
    email character varying(255),
    login character varying(255),
    nom character varying(255) NOT NULL,
    password character varying(255),
    prenom character varying(255) NOT NULL,
    roles character varying(255),
    tel character varying(255),
    token character varying(255),
    tokenexpiration timestamp without time zone
);

CREATE TABLE recolte (
    id bigint NOT NULL,
    commentaire character varying(255),
    date timestamp without time zone,
    poids_miel integer,
    type_miel integer
);

CREATE TABLE recolte_hausse (
    id bigint NOT NULL,
    poids_apres integer,
    poids_avant integer,
    essaim_id bigint,
    hausse_id bigint,
    recolte_id bigint,
    ruche_id bigint,
    rucher_id bigint
);

CREATE TABLE ruche (
    id bigint NOT NULL,
    active boolean,
    commentaire character varying(255),
    date_acquisition date,
    latitude real,
    longitude real,
    nom character varying(255) NOT NULL,
    poids_vide integer,
    essaim_id bigint,
    rucher_id bigint,
    type_id bigint
);

CREATE TABLE ruche_type (
    id bigint NOT NULL,
    nb_cadres_max integer,
    nom character varying(255) NOT NULL
);

CREATE TABLE rucher (
    id bigint NOT NULL,
    actif boolean NOT NULL,
    adresse character varying(255),
    altitude integer,
    commentaire character varying(255),
    depot boolean NOT NULL,
    latitude real,
    longitude real,
    nom character varying(255) NOT NULL,
    ressource character varying(255),
    contact_id bigint,
    dessin character varying
);

ALTER TABLE essaim
    ADD CONSTRAINT essaim_pkey PRIMARY KEY (id);

ALTER TABLE evenement
    ADD CONSTRAINT evenement_pkey PRIMARY KEY (id);

ALTER TABLE hausse
    ADD CONSTRAINT hausse_pkey PRIMARY KEY (id);

ALTER TABLE personne
    ADD CONSTRAINT personne_pkey PRIMARY KEY (id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT recolte_hausse_pkey PRIMARY KEY (id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT recolte_hausse_recolte_id_hausse_id_key UNIQUE (recolte_id, hausse_id);

ALTER TABLE recolte
    ADD CONSTRAINT recolte_pkey PRIMARY KEY (id);

ALTER TABLE ruche
    ADD CONSTRAINT ruche_pkey PRIMARY KEY (id);

ALTER TABLE ruche_type
    ADD CONSTRAINT ruche_type_pkey PRIMARY KEY (id);

ALTER TABLE rucher
    ADD CONSTRAINT rucher_pkey PRIMARY KEY (id);

ALTER TABLE rucher
    ADD CONSTRAINT uk_1jx5wslivv15iu7ciaeg6wtwr UNIQUE (nom);

ALTER TABLE ruche
    ADD CONSTRAINT uk_cvfi8k2gu31nuclrmdueg1pm6 UNIQUE (essaim_id);

ALTER TABLE hausse
    ADD CONSTRAINT uk_hrlrtt93mghrc2raruhvohrt2 UNIQUE (nom);

ALTER TABLE ruche
    ADD CONSTRAINT uk_kjqsbk0l01yxvb6dwrlvrwnq6 UNIQUE (nom);

ALTER TABLE ruche_type
    ADD CONSTRAINT uk_rj98q9fmt7t766k0xwdvp1700 UNIQUE (nom);

ALTER TABLE essaim
    ADD CONSTRAINT uk_s7cpei1xnay5q8enem8htkj0w UNIQUE (nom);

ALTER TABLE hausse
    ADD CONSTRAINT fk3mxijxbjpissl3ivkykdh7qyy FOREIGN KEY (ruche_id) REFERENCES ruche(id);

ALTER TABLE evenement
    ADD CONSTRAINT fk3vvxrb2uw6hhs03jfnv7j17id FOREIGN KEY (ruche_id) REFERENCES ruche(id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT fk4v5dc7edwc8vum0wkwqnd6swx FOREIGN KEY (essaim_id) REFERENCES essaim(id);

ALTER TABLE rucher
    ADD CONSTRAINT fk56x4y61aow8hmqg2bapiyvmag FOREIGN KEY (contact_id) REFERENCES personne(id);

ALTER TABLE ruche
    ADD CONSTRAINT fk89vnochw0ms8g4j10e0by0i43 FOREIGN KEY (rucher_id) REFERENCES rucher(id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT fkbj2l4wfow4ksrnvac5xy9uwo0 FOREIGN KEY (ruche_id) REFERENCES ruche(id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT fkc59g3pd6nd9ao4om9o872toms FOREIGN KEY (hausse_id) REFERENCES hausse(id);

ALTER TABLE ruche
    ADD CONSTRAINT fkg9duh1fei4sn4patf1blyhine FOREIGN KEY (essaim_id) REFERENCES essaim(id);

ALTER TABLE essaim
    ADD CONSTRAINT fkjm960kisa12unpdq36hoqmb88 FOREIGN KEY (souche_id) REFERENCES essaim(id);

ALTER TABLE ruche
    ADD CONSTRAINT fkm4lyusdgut6023tea6v3o31ps FOREIGN KEY (type_id) REFERENCES ruche_type(id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT fkq454nllgj2h9hcy1dqyghf6k2 FOREIGN KEY (rucher_id) REFERENCES rucher(id);

ALTER TABLE evenement
    ADD CONSTRAINT fkq6m67bxrqypb07tkf46ps486n FOREIGN KEY (essaim_id) REFERENCES essaim(id);

ALTER TABLE recolte_hausse
    ADD CONSTRAINT fkrqpnqqodp3qt0dg3qgaes65l5 FOREIGN KEY (recolte_id) REFERENCES recolte(id);

ALTER TABLE evenement
    ADD CONSTRAINT fkstpirfwvuw611v81j463qq41g FOREIGN KEY (hausse_id) REFERENCES hausse(id);

ALTER TABLE evenement
    ADD CONSTRAINT fkteiiphvoy1yqx3muejh3wm3pd FOREIGN KEY (rucher_id) REFERENCES rucher(id);


