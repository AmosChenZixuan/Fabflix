use moviedb;
# add movie
delimiter >

drop procedure if exists add_movie >
create procedure add_movie( title varchar(100), release_year int, director varchar(100), 
starName varchar(100), birthYear int, genre varchar(32) )
entry:begin
	# delcare local vars
    # null if not exist
	declare movieId varchar(10) default getMovieId(title, release_year, director);
    # new star id
    declare starID varchar(10) default getStarId(starName, birthYear);
    # existing or new id (inserted)
    declare genreID int default getGenreId(genre);
    
    # movie existed, do nothing
	if movieId is not null then  
		select movieId;
		leave entry;
    end if;
    # insert new record to tables
	select createNewId(max(id)) into movieId from movies;
	insert into movies value(movieId, title, release_year, director);
	# star not exists, create one
    if starId is null then
		select createNewId(max(id)) into starId from stars;
		insert into stars value(starID, starName, birthYear);
    end if;
    # genre not exists, create one
    if genreId is null then
		select max(id) + 1 into genreId from genres;
        insert into genres value(genreId, genre);
    end if;
    insert into stars_in_movies value(starID, movieId);
	insert into genres_in_movies value(genreId, movieId);

    select "done" as message, movieId, starId, genreId;
end>

drop procedure if exists add_star >
create procedure add_star(starName varchar(100), birthYear int)
begin
	declare starID varchar(10);
	select createNewId(max(id)) into starId from stars;
	insert into stars value(starID, starName, birthYear);
    select starID;
end>

# helper functions
drop function if exists getMovieId >
create function getMovieId( title varchar(100), release_year int, director varchar(100) ) 
returns varchar(10) deterministic
begin
	return (select m.id 
    from movies as m 
    where m.title = title and m.year = release_year and m.director = director limit 1);
end>

drop function if exists getStarId >
create function getStarId( starName varchar(100), birthYear int ) 
returns varchar(10) deterministic
begin
	declare sid varchar(10);
    if birthYear is null then
		select id into sid from stars where name = starName and birthYear is null limit 1;
	else 
		select s.id into sid from stars as s where s.name = starName and s.birthYear = birthYear limit 1;
    end if;
    return sid;
end>

drop function if exists getGenreId >
create function getGenreId( genre varchar(32) ) 
returns varchar(10) deterministic
begin
    return (select id from genres where name = genre limit 1);
end>

drop function if exists createNewId >
create function createNewId( oldId varchar(10) ) returns varchar(10) deterministic
begin
	# increment the numeric part of last largest id
	declare numId varchar(10) default substring(oldId, 3) + 1;
    # put the leading 0 in oldId back
    declare len int default length(oldId)-2;
    while length(numId) < len Do
		set numId = concat('0', numId);
	end while;
    # combine and return
	return concat(substring(oldId, 1, 2), numId);
end>

## xml
drop procedure if exists batch_add_movie>
create procedure batch_add_movie( title varchar(100),
 release_year int, director varchar(100) )
begin
	declare movieId varchar(10) default getMovieId(title, release_year, director);
    if movieId is null then
		select createNewId(max(id)) into movieId from movies;
		insert into movies value(movieId, title, release_year, director);
    end if;
end>

drop procedure if exists batch_add_star >
create procedure batch_add_star( starName varchar(100), birthYear int )
begin
	declare starID varchar(10);
    select id into starId from stars where name = starName limit 1;
    if starId is null then
		select createNewId(max(id)) into starId from stars;
		insert into stars value(starID, starName, birthYear);
	end if;
end>

drop procedure if exists batch_add_genre>
create procedure batch_add_genre( genre varchar(32) )
begin
# calling this procedure, we must already know that genre did not exist
	declare genreId int;
	select max(id) + 1 into genreId from genres;
	insert into genres value(genreId, genre);
end>

drop procedure if exists batch_link_genre>
create procedure batch_link_genre( title varchar(100),
release_year int, director varchar(100),
genre varchar(32) )
begin
	declare movieId varchar(10) default getMovieId(title, release_year, director);
	declare genreId int;
    select id into genreId from genres where name = genre limit 1;
    if not (movieId is null OR genreId is null) then
		if not exists(select 1 from genres_in_movies as gim 
				where gim.genreId = genreId and gim.movieId = movieId) then
			insert into genres_in_movies value(genreId, movieId); 
		end if;
    end if;
end>

drop procedure if exists batch_add_and_link_genre >
create procedure batch_add_and_link_genre( title varchar(100),
release_year int, director varchar(100),
genre varchar(32) )
begin
	declare movieId varchar(10) default getMovieId(title, release_year, director);
	declare genreId int;
    select id into genreId from genres where name = genre limit 1;
    if genreId is null then 
		select max(id) + 1 into genreId from genres;
		insert into genres value(genreId, genre);
	end if;
    if movieId is not null then
		if not exists(select 1 from genres_in_movies as gim 
				where gim.genreId = genreId and gim.movieId = movieId) then
			insert into genres_in_movies value(genreId, movieId); 
		end if;
    end if;
end>

drop procedure if exists batch_link_star>
create procedure batch_link_star( title varchar(100),
release_year int, director varchar(100),
starName varchar(100) )
begin 
	declare movieId varchar(10) default getMovieId(title, release_year, director);
    declare starID varchar(10);
    select id into starID from stars where name = starName order by id desc limit 1;
    if movieId is not null AND starID is not null then
		if not exists(select 1 from stars_in_movies as sim 
				where sim.starId = starId and sim.movieId = movieId) then
			insert into stars_in_movies value(starID, movieId);
		end if;
    end if;
end>
delimiter ;