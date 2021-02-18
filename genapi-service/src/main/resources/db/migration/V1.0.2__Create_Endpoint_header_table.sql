create table EndpointHeader
(
    Id           int primary key auto_increment,
    EndpointId   int          not null,
    Name         varchar(128) not null,
    Value        varchar(512) not null,
    EndpointType varchar(64)  not null
)