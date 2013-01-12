--the script is written in java db - also commonly known as derby
--the dialect of sql which is widely described in the internet documentation
--and which can run in two modes - either as a server or as a tool library - we choose the latter
--by saying URN we mean unique random numbers
create table ballotBox(
	urn varchar(20) unique,
	vote varchar(200) --well - that big because the vote itself will have to contain L commision's digital signature
)