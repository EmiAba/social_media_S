package app.scheduler;

import app.message.service.MessageService;
import app.user.model.User;
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

    @Scheduled(cron = "0 0 20 * * TUE")
    public void sendWeeklySummaryToInbox() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String summary = engagementSummaryService.generateEngagementSummary(user);


            messageService.saveMessage(user.getUsername(), summary, null);
        }

        System.out.println("Sent weekly engagement summary messages.");
    }
}