package app.scheduler;

import app.message.service.MessageService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repoistory.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
@RequiredArgsConstructor
public class EngagementSummaryScheduler {

    private final EngagementSummaryService engagementSummaryService;
    private final MessageService messageService;
    private final UserRepository userRepository;



    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklySummaryToInbox() {
        System.out.println("Scheduler is called");

        User systemAdmin = userRepository.findUserByRole(UserRole.ADMIN);

        if (systemAdmin == null) {
            System.out.println("No system admin found. Skipping engagement summary.");
            return;
        }

        List<User> users = userRepository.findAll();

        for (User user : users) {
            String summary = engagementSummaryService.generateEngagementSummary(user);
            messageService.saveMessage(user.getUsername(), summary, systemAdmin);
        }

        System.out.println("Sent weekly engagement summary messages.");
    }
}
