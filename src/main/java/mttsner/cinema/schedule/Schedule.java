package mttsner.cinema.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import mttsner.cinema.movies.Movies;


@Entity
@Getter
@Setter
public class Schedule {

    @Id
    @Column(nullable = false, updatable = false)
    @SequenceGenerator(
            name = "primary_sequence",
            sequenceName = "primary_sequence",
            allocationSize = 1,
            initialValue = 10000
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "primary_sequence"
    )
    private Integer sessionId;

    @Column
    private OffsetDateTime startTime;

    @Column
    private String maxSeats;

    @Column
    private Integer freeSeats;

    @ManyToOne()
    @JoinColumn(name = "movie_id", nullable = false)
    private Movies movieId;

}
