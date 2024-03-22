package mttsner.cinema.schedule;

import mttsner.cinema.movies.AgeRating;
import mttsner.cinema.movies.Genre;
import mttsner.cinema.movies.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    @Query("SELECT sch FROM Schedule sch " +
            "JOIN sch.movie.genres genre " +
            "WHERE sch.startTime >= NOW() " +
            "AND CAST(sch.startTime as DATE) = :date " +
            "AND (:genres IS NULL OR genre IN :genres) " +
            "AND (:ages is NULL OR sch.movie.ageRating IN :ages) " +
            "AND (:languages IS NULL OR sch.movie.language IN :languages) " +
            "AND (:times IS NULL OR FORMATDATETIME(sch.startTime, 'HH:mm')  IN :times) " +
            "ORDER BY sch.startTime")
    List<Schedule> findByFilters(
            LocalDate date,
            List<Genre> genres,
            List<AgeRating> ages,
            List<Language> languages,
            List<String> times
    );

    @Query("SELECT sch FROM Schedule sch " +
            "WHERE sch.startTime >= NOW() " +
            "AND CAST(sch.startTime as DATE) = :date " +
            "ORDER BY sch.startTime")
    List<Schedule> findByDate(LocalDate date);
}
