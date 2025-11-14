package com.clipers.clipers.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.clipers.clipers.entity.User;

/**
 * Servicio que implementa Observer Pattern implícitamente
 * para el sistema de notificaciones
 */
@Service
public class NotificationService {

    // Lista de observadores (diferentes tipos de notificación)
    private final List<NotificationHandler> handlers;

    public NotificationService() {
        this.handlers = new ArrayList<>();
        // Registrar diferentes tipos de manejadores
        this.handlers.add(new EmailNotificationHandler());
        this.handlers.add(new InAppNotificationHandler());
        this.handlers.add(new PushNotificationHandler());
    }

    // Observer pattern - notificar a todos los observadores
    public void sendWelcomeNotification(User user) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.USER_REGISTERED,
            user.getId(),
            null,
            null,
            "¡Bienvenido a Clipers, " + user.getFirstName() + "!"
        );
        notifyAllHandlers(event);
    }

    public void notifyPostLiked(String postOwnerId, String likerId, String postId) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.POST_LIKED,
            postOwnerId,
            likerId,
            postId,
            "Tu publicación recibió un nuevo like"
        );
        notifyAllHandlers(event);
    }

    public void notifyPostCommented(String postOwnerId, String commenterId, String postId, String commentText) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.POST_COMMENTED,
            postOwnerId,
            commenterId,
            postId,
            "Nuevo comentario: " + commentText
        );
        notifyAllHandlers(event);
    }

    public void notifyJobMatched(String candidateId, String jobId, double matchScore) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.JOB_MATCHED,
            candidateId,
            null,
            jobId,
            String.format("Nueva oportunidad laboral con %.0f%% de compatibilidad", matchScore * 100)
        );
        notifyAllHandlers(event);
    }

    public void notifyCliperProcessed(String userId, String cliperId) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.CLIPER_PROCESSED,
            userId,
            null,
            cliperId,
            "Tu Cliper ha sido procesado y está listo para mostrar"
        );
        notifyAllHandlers(event);
    }

    public void notifyJobApplication(String jobId, String applicantId) {
        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.JOB_APPLICATION,
            null, // La empresa será notificada a través del job
            applicantId,
            jobId,
            "Nueva aplicación recibida para tu oferta laboral"
        );
        notifyAllHandlers(event);
    }

    public void notifyApplicationStatusUpdate(String candidateId, String jobId, com.clipers.clipers.entity.JobMatch.ApplicationStatus status) {
        String message = switch (status) {
            case ACCEPTED -> "¡Felicitaciones! Tu aplicación ha sido aceptada.";
            case REJECTED -> "Tu aplicación ha sido rechazada.";
            case PENDING -> "El estado de tu aplicación ha cambiado.";
        };

        NotificationEvent event = new NotificationEvent(
            NotificationEvent.EventType.APPLICATION_STATUS_UPDATE,
            candidateId,
            null,
            jobId,
            message
        );
        notifyAllHandlers(event);
    }

    private void notifyAllHandlers(NotificationEvent event) {
        for (NotificationHandler handler : handlers) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                System.err.println("Error en handler de notificaciones: " + e.getMessage());
            }
        }
    }

    // Interfaz para los manejadores de notificaciones
    private interface NotificationHandler {
        void handle(NotificationEvent event);
    }

    // Implementaciones concretas de los manejadores (Observer pattern)
    private static class EmailNotificationHandler implements NotificationHandler {
        @Override
        public void handle(NotificationEvent event) {
            // Simular envío de email
            System.out.println(String.format(
                "📧 EMAIL para usuario %s: %s [%s]", 
                event.getUserId(), 
                event.getMessage(),
                event.getType()
            ));
        }
    }

    private static class InAppNotificationHandler implements NotificationHandler {
        @Override
        public void handle(NotificationEvent event) {
            // Simular notificación in-app
            System.out.println(String.format(
                "🔔 IN-APP NOTIFICATION para usuario %s: %s [%s]", 
                event.getUserId(), 
                event.getMessage(),
                event.getType()
            ));
        }
    }

    private static class PushNotificationHandler implements NotificationHandler {
        @Override
        public void handle(NotificationEvent event) {
            // Simular push notification
            System.out.println(String.format(
                "📱 PUSH NOTIFICATION para usuario %s: %s [%s]", 
                event.getUserId(), 
                event.getMessage(),
                event.getType()
            ));
        }
    }

    // Clase interna para eventos de notificación
    public static class NotificationEvent {
        public enum EventType {
            USER_REGISTERED, POST_LIKED, POST_COMMENTED, JOB_MATCHED, CLIPER_PROCESSED, JOB_APPLICATION, APPLICATION_STATUS_UPDATE
        }
        
        private final EventType type;
        private final String userId;
        private final String actorId;
        private final String entityId;
        private final String message;
        private final LocalDateTime timestamp;
        
        public NotificationEvent(EventType type, String userId, String actorId, String entityId, String message) {
            this.type = type;
            this.userId = userId;
            this.actorId = actorId;
            this.entityId = entityId;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public EventType getType() { return type; }
        public String getUserId() { return userId; }
        public String getActorId() { return actorId; }
        public String getEntityId() { return entityId; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
