# --- !Ups

CREATE TABLE Post (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    link varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    body text NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs
DROP TABLE Post;
