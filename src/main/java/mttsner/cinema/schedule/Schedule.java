package mttsner.cinema.schedule;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import mttsner.cinema.movies.Movie;



@Entity
@Getter
@Setter
public class Schedule {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sessionId;

    @Column(nullable = false)
    private OffsetDateTime startTime;

    @Column(nullable = false)
    private Integer maxSeats;

    @Column(nullable = false)
    private Integer freeSeats;

    @ManyToOne()
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movieId;

    @Column(nullable = false)
    private Integer Rows;

    @Column(nullable = false)
    private Integer Columns;


    @ElementCollection(fetch = FetchType.EAGER)
    private List<SeatStatus> seats;

    @Version
    private Long version;

    public int getSeatId(int row, int column) {
        return row * this.Columns + column;
    }

    public SeatStatus getSeat(int row, int column) {
        return this.seats.get(getSeatId(row, column));
    }

    public void setSeat(int row, int column, SeatStatus status) {
        this.seats.set(getSeatId(row, column), status);
    }
}
