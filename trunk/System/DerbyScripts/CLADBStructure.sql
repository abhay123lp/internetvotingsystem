--the script is written in java db - also commonly known as derby
--the dialect of sql which is widely described in the internet documentation
--and which can run in two modes - either as a server or as a tool library - we choose the latter
--by saying URN we mean unique random numbers
create table CLATable(
	pesel varchar(20) not null, --this is unique user id (like pesel in Poland)
	password varchar(20) not null, --this can be everything - from public key, to symetric key in a form of either raw text or its hash
	urn varchar(20),
	primary key (pesel)
);

--insert into CLATable values ('1234567890', '123456', null)

--update CLATable set urn='839293402' where pesel='1234567890'