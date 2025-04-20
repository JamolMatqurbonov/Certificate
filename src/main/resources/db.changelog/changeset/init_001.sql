CREATE TABLE if NOT exists certificate
(
    id           serial primary key,
    first_name   varchar(255) not null,
    last_name    varchar(255) not null,
    course       varchar(255) not null,
    file_path    varchar(255) not null,
    qr_id        varchar(255) not null,
    u_id         int not null,
    given_at   timestamp default now()
    );