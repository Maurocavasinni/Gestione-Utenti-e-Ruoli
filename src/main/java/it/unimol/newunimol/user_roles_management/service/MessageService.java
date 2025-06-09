package it.unimol.newunimol.user_roles_management.service;

import it.unimol.newunimol.user_roles_management.dto.UserDto;
import it.unimol.newunimol.user_roles_management.dto.UserProfileDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.users}")
    private String usersExchange;

    public void publishUserCreated(UserDto user) {
        Map<String, Object> message = createUserMessage(user, "USER_CREATED");
        rabbitTemplate.convertAndSend(usersExchange, "user.created", message);
    }

    public void publishUserUpdated(UserDto user) {
        Map<String, Object> message = createUserMessage(user, "USER_UPDATED");
        rabbitTemplate.convertAndSend(usersExchange, "user.updated", message);
    }

    public void publishUserDeleted(String userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "USER_DELETED");
        message.put("userId", userId);
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(usersExchange, "user.deleted", message);
    }

    public void publishRoleAssigned(String userId, String roleId) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "ROLE_ASSIGNED");
        message.put("userId", userId);
        message.put("roleId", roleId);
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(usersExchange, "role.assigned", message);
    }

    public void publishProfileUpdated(UserProfileDto profile) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", "PROFILE_UPDATED");
        message.put("userId", profile.id());
        message.put("username", profile.username());
        message.put("email", profile.email());
        message.put("name", profile.nome());
        message.put("surname", profile.cognome());
        message.put("timestamp", System.currentTimeMillis());
        rabbitTemplate.convertAndSend(usersExchange, "user.updated", message);
    }

    private Map<String, Object> createUserMessage(UserDto user, String eventType) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("userId", user.id());
        message.put("username", user.username());
        message.put("email", user.email());
        message.put("name", user.name());
        message.put("surname", user.surname());
        message.put("roleId", user.ruolo().id());
        message.put("roleName", user.ruolo().nome());
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }
}
