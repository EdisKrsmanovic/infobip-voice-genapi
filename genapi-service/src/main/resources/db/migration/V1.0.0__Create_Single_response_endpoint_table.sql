create table SingleResponseEndpoint
(
    Id         int primary key auto_increment,
    HttpMethod varchar(20)   not null,
    Response   varchar(2048) not null
)