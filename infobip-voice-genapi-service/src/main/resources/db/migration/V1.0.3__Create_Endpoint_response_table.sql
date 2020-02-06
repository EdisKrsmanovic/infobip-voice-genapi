create table dbo.EndpointResponse
(
    Id            int identity
        constraint PK_ResponseEndpoint
            primary key,
    EndpointId    int           not null
        constraint EndpointResponse_ScenarioEndpoint_Id_fk
            references ScenarioEndpoint,
    Body      varchar(2048) not null,
    OrdinalNumber int           not null
)
