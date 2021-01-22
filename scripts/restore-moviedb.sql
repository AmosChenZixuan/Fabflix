# restore original records
delete from genres_in_movies where genreId > 23 or movieId > 'tt0499469';
delete from stars_in_movies where starId > 'nm9423080' or movieId > 'tt0499469';
delete from genres where id > 23;
alter table genres AUTO_INCREMENT = 23;
delete from stars where id > 'nm9423080';
delete from movies where id > 'tt0499469';