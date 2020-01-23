create table SingleResponseEndpoint
(
    Id         int identity
        constraint PK_SingleResponseEndpoint
            primary key,
    HttpMethod varchar(20)   not null,
    Response   varchar(2048) not null
)