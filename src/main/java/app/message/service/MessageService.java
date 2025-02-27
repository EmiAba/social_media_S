package app.message.service;

import app.exceprion.DomainException;
import app.message.model.Message;
import app.message.repository.MessageRepository;
import app.notification.service.NotificationService;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserService userService;


    @Autowired
    public MessageService(
            MessageRepository messageRepository,
            UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;

    }

    public List<Message> getReceivedMessages(UUID userId) {
        List<Message> messages = messageRepository.findByReceiverId(userId);
        messages.sort(Comparator.comparing(Message::getTimestamp).reversed());
        return messages;
    }

    public Message getMessageById(UUID messageId) {
        Message message = messageRepository.findById(messageId).orElse(null);
        if (message != null) {
            message.setRead(true);
            messageRepository.save(message);
        }
        return message;
    }

    public void saveMessage(String receiverUsername, String content, User sender) {
        if (receiverUsername == null || receiverUsername.trim().isEmpty()) {
            throw new DomainException("Receiver username cannot be empty");
        }

        Optional<User> receiverOptional = userService.getUserByUsername(receiverUsername);
        if (receiverOptional.isEmpty()) {
            throw new DomainException("Receiver not found");
        }

        User receiver = receiverOptional.get();

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        messageRepository.save(message);



    }

    public void deleteMessage(UUID messageId, UUID userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isPresent()) {
            Message message = messageOpt.get();
            if (message.getReceiver().getId().equals(userId)) {
                messageRepository.deleteById(messageId);
            } else {
                throw new DomainException("You can only delete messages from your inbox.");
            }
        } else {
            throw new DomainException("Message not found.");
        }
    }



    public boolean hasUnreadMessages(UUID userId) {
        return getUnreadMessageCount(userId) > 0;
    }

    public int getUnreadMessageCount(UUID userId) {
        return (int) messageRepository.findByReceiverId(userId)
                .stream()
                .filter(message -> !message.isRead())
                .count();
    }

    public List<Message> getReceivedMessagesAndMarkAsRead(UUID userId) {
        List<Message> messages = messageRepository.findByReceiverId(userId);
        messages.sort(Comparator.comparing(Message::getTimestamp).reversed());

        boolean updated = false;
        for (Message message : messages) {
            if (!message.isRead()) {
                message.setRead(true);
                updated = true;
            }
        }

        if (updated) {
            messageRepository.saveAll(messages);
        }

        return messages;
    }
}