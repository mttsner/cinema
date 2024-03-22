package mttsner.cinema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mttsner.cinema.movies.Movie;
import mttsner.cinema.movies.MovieRepository;
import mttsner.cinema.schedule.Schedule;
import mttsner.cinema.schedule.ScheduleRepository;
import mttsner.cinema.schedule.SeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class GenerateExampleData implements ApplicationRunner {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Read movies from json file
        ObjectMapper objectMapper = new ObjectMapper();
        List<Movie> movies = objectMapper.readValue(new File("./movies.json"), new TypeReference<>() {});
        // Save movies from json file to db
        movieRepository.saveAll(movies);
        // Get current date time at full hour
        OffsetDateTime time = OffsetDateTime.now().withMinute(0).withSecond(0);
        // Generate movie schedule for the week
        for (int i = 0; i < 7; i++) {
            generateScheduleForDay(time.plusDays(i), movies);
        }
    }

    private void generateScheduleForDay(OffsetDateTime time, List<Movie> movies) {
        Random rng = new Random();
        // Create random schedule item for each hour from 8-23
        for (int i = 8; i < 24; i++) {
            Schedule schedule = new Schedule();
            schedule.setStartTime(time.withHour(i));
            // Pick random movie for the schedule item
            schedule.setMovie(movies.get(rng.nextInt(movies.size())));
            // Generate random reserved seats in a random hall
            generateSeating(schedule);
            // Save schedule item to db
            scheduleRepository.save(schedule);
        }
    }

    private void generateSeating(Schedule schedule) {
        Random rng = new Random();
        switch (rng.nextInt(3)) {
            case 0: // Big
                generateHall(schedule, 10, 10);
                generateReservedSeats(schedule);
                break;
            case 1: // Medium
                generateHall(schedule, 7, 6);
                generateReservedSeats(schedule);
                break;
            case 2: // Small
                generateHall(schedule, 5, 5);
                // Make upper row either side seat be invalid
                schedule.setSeat(4,0, SeatStatus.Invalid);
                schedule.setSeat(4,4, SeatStatus.Invalid);
                // Set correct seat count
                schedule.setMaxSeats((5*5)-2);
                schedule.setFreeSeats((5*5)-2);
                generateReservedSeats(schedule);
        }
    }

    private void generateHall(Schedule schedule, int rows, int cols) {
        // Configure hall size
        schedule.setMaxSeats(rows*cols);
        schedule.setFreeSeats(rows*cols);
        schedule.setRows(rows);
        schedule.setColumns(cols);
        // Generate empty seats
        schedule.setSeats(new ArrayList<>(Collections.nCopies(rows*cols, SeatStatus.Available)));
    }

    private void generateReservedSeats(Schedule schedule) {
        Random rng = new Random();
        int reserved = rng.nextInt(1, schedule.getMaxSeats());
        for (int i = 0; i < reserved; i++) {
            // Generate random row and column index
            int row = rng.nextInt(schedule.getRows());
            int col = rng.nextInt(schedule.getColumns());
            // Prevent collisions when generating random seating
            if (schedule.getSeat(row, col) == SeatStatus.Available) {
                schedule.setSeat(row, col, SeatStatus.Reserved);
                schedule.setFreeSeats(schedule.getFreeSeats()-1);
            }
        }
    }
}
