CREATE TABLE member(
                       id BIGSERIAL PRIMARY KEY ,
                       name TEXT NOT NULL ,
                       email TEXT NOT NULL ,
                       password TEXT NOT NULL,
                       age INT NOT NULL,
                       gender TEXT DEFAULT 'NA',
                       CONSTRAINT member_email_uniq UNIQUE (email)
);