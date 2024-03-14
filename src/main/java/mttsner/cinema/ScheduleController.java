package mttsner.cinema;

import mttsner.cinema.movies.AgeRating;
import mttsner.cinema.movies.Genre;
import mttsner.cinema.movies.Language;
import mttsner.cinema.schedule.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class ScheduleController
{
    @Autowired
    private ScheduleRepository scheduleRepository;

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
}
