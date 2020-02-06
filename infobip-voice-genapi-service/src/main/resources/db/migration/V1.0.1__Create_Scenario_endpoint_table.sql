create table dbo.ScenarioEndpoint
(
    Id         int identity
        constraint PK_ScenarioEndpoint
            primary key,
    HttpMethod varchar(20) not null
)