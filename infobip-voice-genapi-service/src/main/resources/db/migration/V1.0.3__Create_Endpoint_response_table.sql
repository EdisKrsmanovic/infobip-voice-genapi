create table EndpointResponse
(
    Id            int primary key,
    EndpointId    int           not null,
    Body          varchar(2048) not null,
    OrdinalNumber int           not null
)
