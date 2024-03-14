package mttsner.cinema;

import mttsner.cinema.schedule.Schedule;
import mttsner.cinema.schedule.ScheduleRepository;
import mttsner.cinema.schedule.SeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SeatsController {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/seats")
    public String seats(@RequestParam Integer session,
                        @RequestParam Integer adult,
                        @RequestParam Integer student,
                        @RequestParam Integer child,
                        Model model) {
        Schedule schedule = scheduleRepository.findById(session).orElseThrow();
        // Check if ticket count is valid
        int tickets = adult + student + child;
        if (tickets <= 0 || tickets > schedule.getFreeSeats()) {
            // Keeps the user on the ticket choice page
            return "redirect:/tickets?session=" + session;
        }
        // Generate recommended seat positions in the cinema
        generateRecommendedSeats(schedule, tickets);
        // Send attributes to thymeleaf
        model.addAttribute("schedule", schedule);
        model.addAttribute("sessionId", session);
        return "seats/index";
    }

    private int[] findSeats(List<SeatStatus> seats, int needed) {
        int count = 0; // How many seats have been available in a row
        int pos = -1; // Position of the first recommended seat
        // TODO: How algorithm works!
        for (int i = 0; i < seats.size(); i++) {
            // If seat is available increment count and continue to next iteration
            if (seats.get(i) == SeatStatus.Available) {
                count++;
            } else { // If seat isn't available, reset counter
                count = 0;
            }
            // If enough seats have been available, save the position of the current seat
            if (count >= needed) {
                pos = i;
            }
        }
        // Return state for later processing
        return new int[]{pos, count};
    }

    private int recommendSeats(List<SeatStatus> row, int needed) {
        // Split row in half
        List<SeatStatus> firstHalf = row.subList(0, row.size() / 2);
        List<SeatStatus> secondHalf = row.subList(row.size() / 2, row.size());
        // Find best seats in each half
        int[] first = findSeats(firstHalf, needed);
        int[] second = findSeats(secondHalf.reversed(), needed);
        //if (needed == 1) {
        //    return first[0] >= second[0] ? first[0] : row.size() - second[0] - 1;
        //}
        // If middle is available, return middle seats
        if (first[1] + second[1] >= needed) {
            int neededFirst = (needed / 2 + needed % 2);
            int neededSecond = needed / 2;
            int firstOver = Math.max(0, neededFirst - first[1]);
            return firstHalf.size() - 1 + (neededSecond + firstOver);
        }
        // Return seats with the best ranking
        return first[0] >= second[0] ? first[0] : (row.size() - 1 - second[0]) + needed - 1;
    }

    private void generateRecommendedSeats(Schedule schedule, int needed) {
        List<SeatStatus> seats = schedule.getSeats();
        int[] recommended = new int[schedule.getRows()];
        // Find best seats in each row
        for (int row = 0; row < schedule.getRows(); row++) {
            // Get view of the current row
            List<SeatStatus> sub = seats.subList(row * schedule.getColumns(), (1 + row) * schedule.getColumns());
            // Get recommended seats in current row
            recommended[row] = recommendSeats(sub, needed);
        }
        // Find best row
        int idx = 0;
        int half = schedule.getRows() / 2;
        for (int row = half; row < schedule.getRows() && row >= 0; row = half + idx) {
            // Move from middle row to the outer rows
            if (idx <= 0) {
                idx *= -1; // Flip sign
                idx++; // Increment
            } else {
                idx *= -1; // Flip sign
            }
            // Check if current row has a seating recommendation
            if (recommended[row] == -1) {
                continue;
            }
            // Check if we should use the next best rows seating (not the best because only 1 level of depth)
            if (half+idx < schedule.getRows() &&
                half+idx >= 0 && recommended[half+idx] != -1 &&
                compareSeatScore(recommended[row] - half, recommended[half+idx] - half, needed))
            {
                checkSeats(schedule, needed, recommended[half+idx], half+idx);
                return;
            }
            // Use current rows seating
            checkSeats(schedule, needed, recommended[row], row);
            return;
        }
        // If no recommended seats were found, check any available seat
        int checked = 0;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i) == SeatStatus.Available) {
                seats.set(i, SeatStatus.Checked);
                checked++;
            }
            // All needed seats have been checked, exit
            if (checked == needed) {
                break;
            }
        }
    }

    private static boolean compareSeatScore(int pos1, int pos2, int seats) {
        // Threshold determines how far apart seat positions can be, before choosing the second position
        int threshold = 2;
        return Math.abs(pos1 - seats / 2) - Math.abs(pos2 - seats / 2) > threshold;
    }

    private static void checkSeats(Schedule schedule, int needed, int col, int row) {
        for (int i = col; i > col - needed; i--) {
            schedule.setSeat(row, i, SeatStatus.Checked);
        }
    }
}
