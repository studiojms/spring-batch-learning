CREATE TABLE users(
   username    VARCHAR(9) NOT NULL PRIMARY KEY,
   login_email VARCHAR(18),
   identifier  INTEGER  NOT NULL,
   first_name  VARCHAR(6) NOT NULL,
   last_name   VARCHAR(7) NOT NULL
);

INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('booker12','rachel@example.com',9012,'Rachel','Booker');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('grey07',NULL,2070,'Laura','Grey');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('johnson81',NULL,4081,'Craig','Johnson');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('jenkins46','mary@example.com',9346,'Mary','Jenkins');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('smith79','jamie@example.com',5079,'Jamie','Smith');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('arthur1','arthur@mail.com',5244,'Arthur','Rabbit');
INSERT INTO users(username,login_email,identifier,first_name,last_name) VALUES ('asdf2','asdf2@mail.com',1123,'James','Gunn');