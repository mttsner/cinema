package mttsner.cinema.movies;

import org.springframework.data.jpa.repository.JpaRepository;


public interface MoviesRepository extends JpaRepository<Movies, Integer> {
}
