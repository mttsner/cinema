package mttsner.cinema.controller;

import mttsner.cinema.movies.AgeRating;
import mttsner.cinema.movies.Genre;
import mttsner.cinema.movies.Language;
import mttsner.cinema.movies.Movie;
import mttsner.cinema.schedule.Schedule;
import mttsner.cinema.schedule.ScheduleRepository;
import mttsner.cinema.schedule.SeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class HomeController {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/schedule";
    }

    @GetMapping("/tickets")
    public String tickets(@RequestParam Integer session, Model model) {
        model.addAttribute("event", session);
        return "tickets/index";
    }

    @PostMapping("/buy")
    public String buy(@RequestParam Integer session,
                      @RequestParam int[] seat,
                      Model model) {
        Schedule schedule = scheduleRepository.findById(session).orElseThrow();
        List<SeatStatus> seats = schedule.getSeats();
        // Add bought seats
        for (int id : seat) {
            if (seats.get(id) == SeatStatus.Available) {
                seats.set(id, SeatStatus.Reserved);
            } else {
                throw new ErrorResponseException(HttpStatus.CONFLICT);
            }
        }
        // Update seats with new reserved seats
        schedule.setSeats(seats);
        // Update the number of available seats
        schedule.setFreeSeats(schedule.getFreeSeats() - seat.length);
        // Update schedule in the database
        scheduleRepository.save(schedule);
        // Redirect to the main page
        model.addAttribute("movieId", schedule.getMovieId().getMovieId());
        return "buy/index";
    }
}
