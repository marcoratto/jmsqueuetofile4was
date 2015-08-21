Source->MQ Server Source: Put a message
note right of Source:
A source program writes
a message on a queue
end note
MQ Server Source->MQ Server Target: Trasmission
MQ Server Target->Websphere: JMS Listener
activate Websphere
Websphere->EJB MDB: Wake up
activate EJB MDB
EJB MDB->EJB MDB: Get the message
EJB MDB->Target: Write the message
note left of Target:
The EJB MDB gets the message
and writes it on the file system
of the server
end note
deactivate EJB MDB
deactivate Websphere