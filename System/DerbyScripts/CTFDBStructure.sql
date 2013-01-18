create table validList(
	urn varchar(20) primary key,
	used int
);

create table votes(
	id varchar(20) primary key,
	vote int
);

create table candidates(
	c_id int primary key,
	first_name varchar(20),
	last_name varchar(20)
);