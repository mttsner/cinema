package mttsner.cinema;

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
        return "home/tickets";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        LocalDate today = LocalDate.now();
        // Calculate dates for each day in the week for the navigation menu
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dates.add(today.plusDays(i));
        }
        model.addAttribute("dates", dates);
        // Format date names in estonian for the navigation menu
        DateTimeFormatter locale = DateTimeFormatter.ofPattern("E", Locale.forLanguageTag("et"));
        model.addAttribute("locale", locale);
        // Find all schedule items for today
        model.addAttribute("schedule", scheduleRepository.findByDate(today));
        // Render page
        return "schedule/index";
    }

    @PostMapping("/filter")
    public String filter(@RequestParam(required = false) List<Genre> genres,
                         @RequestParam(required = false) List<AgeRating> ages,
                         @RequestParam(required = false) List<Language> languages,
                         @RequestParam(required = false) List<String> times,
                         @RequestParam LocalDate date,
                         Model model) {
        model.addAttribute("schedule", scheduleRepository.findByFilters(date, genres, ages, languages, times));
        return "schedule/index::items";
    }

    @PostMapping("/buy")
    public String buy(@RequestParam Integer session,
                      @RequestParam int[] seat) {
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
        return "redirect:/schedule";
    }
}
