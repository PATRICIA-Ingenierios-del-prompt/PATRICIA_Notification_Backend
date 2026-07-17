package ingprompt.patricia.notification.infrastructure.messaging.listener;

import ingprompt.patricia.notification.application.port.in.ReceiveNotificationCase;
import ingprompt.patricia.notification.domain.enums.NotificationType;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventCreatedEvent;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventLinkedToParcheEvent;
import ingprompt.patricia.notification.infrastructure.messaging.event.EventoEnvelope;
import ingprompt.patricia.notification.infrastructure.messaging.event.LogroDesbloqueadoPayload;
import ingprompt.patricia.notification.infrastructure.messaging.event.MatchConfirmadoPayload;
import ingprompt.patricia.notification.infrastructure.messaging.event.MessageCreatedEvent;
import ingprompt.patricia.notification.infrastructure.messaging.event.ParcheCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationListenersTest {

    @Mock
    private ReceiveNotificationCase receiveNotification;

    // ---- ParcheEventsListener (#1) ----

    @Test
    void parcheCreated_public_broadcasts() {
        ParcheEventsListener listener = new ParcheEventsListener(receiveNotification);
        UUID parcheId = UUID.randomUUID();

        listener.onParcheCreated(new ParcheCreatedEvent("evt", parcheId, "Salsa", "PUBLIC", UUID.randomUUID()));

        verify(receiveNotification).notifyEveryone(eq(NotificationType.NEW_PUBLIC_PARCHE), any(), any(), eq("evt"));
    }

    @Test
    void parcheCreated_private_isIgnored() {
        ParcheEventsListener listener = new ParcheEventsListener(receiveNotification);

        listener.onParcheCreated(new ParcheCreatedEvent("evt", UUID.randomUUID(), "Secret", "PRIVATE", UUID.randomUUID()));

        verifyNoInteractions(receiveNotification);
    }

    // ---- EventEventsListener (#2, #3) ----

    @Test
    void eventCreated_standalone_broadcasts() {
        EventEventsListener listener = new EventEventsListener(receiveNotification);

        listener.onEventCreated(new EventCreatedEvent("evt", UUID.randomUUID(), "Hike", UUID.randomUUID(), false));

        verify(receiveNotification).notifyEveryone(eq(NotificationType.NEW_EVENT_FOR_PUBLIC), any(), any(), eq("evt"));
    }

    @Test
    void eventCreated_linkedToParche_isIgnored() {
        EventEventsListener listener = new EventEventsListener(receiveNotification);

        listener.onEventCreated(new EventCreatedEvent("evt", UUID.randomUUID(), "Hike", UUID.randomUUID(), true));

        verify(receiveNotification, never()).notifyEveryone(any(), any(), any(), any());
    }

    @Test
    void eventLinkedToParche_notifiesMembers() {
        EventEventsListener listener = new EventEventsListener(receiveNotification);
        Set<UUID> members = Set.of(UUID.randomUUID(), UUID.randomUUID());

        listener.onEventLinkedToParche(new EventLinkedToParcheEvent(
                "evt", UUID.randomUUID(), "Hike", UUID.randomUUID(), "Mountain Crew", members));

        verify(receiveNotification).notifyUsers(eq(members), eq(NotificationType.NEW_EVENT_IN_PARCHE), any(), any(), eq("evt"));
    }

    // ---- MatchingEventsListener (#5) ----

    @Test
    void matchConfirmado_notifiesBothUsers() {
        MatchingEventsListener listener = new MatchingEventsListener(receiveNotification);
        UUID recipient = UUID.randomUUID();
        MatchConfirmadoPayload payload = new MatchConfirmadoPayload(UUID.randomUUID(), UUID.randomUUID(), 0.87);
        EventoEnvelope<MatchConfirmadoPayload> envelope =
                new EventoEnvelope<>("evt", Instant.now(), recipient, "match.confirmado", payload);

        listener.onMatchConfirmado(envelope);

        verify(receiveNotification).notifyUser(eq(recipient), eq(NotificationType.NEW_MATCH_CONFIRMED), any(), any(), eq("evt"));
    }

    // ---- LogroEventsListener (#6) ----

    @Test
    void logroDesbloqueado_notifiesUser() {
        LogroEventsListener listener = new LogroEventsListener(receiveNotification);
        UUID userId = UUID.randomUUID();
        LogroDesbloqueadoPayload payload = new LogroDesbloqueadoPayload("MONA_CODER", "Mona Coder", 50, 150);
        EventoEnvelope<LogroDesbloqueadoPayload> envelope =
                new EventoEnvelope<>("evt", Instant.now(), userId, "logro.desbloqueado", payload);

        listener.onLogroDesbloqueado(envelope);

        verify(receiveNotification).notifyUser(eq(userId), eq(NotificationType.ALBUM_MONA_UNLOCKED), any(), any(), eq("evt"));
    }

    // ---- MessageEventsListener (#4 — deferred stub) ----

    @Test
    void messageCreated_notifiesRecipients() {
        UUID sender = UUID.randomUUID();
        UUID recipient = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        Set<String> recipients = Set.of(recipient.toString());

        MessageEventsListener listener = new MessageEventsListener(receiveNotification);

        listener.onMessageCreated(new MessageCreatedEvent(
                "evt", chatId, chatId, "Crew", sender, "juandc", "Hola!", "TEXT", recipients));

        verify(receiveNotification).notifyUsers(eq(Set.of(recipient)),
                eq(NotificationType.NEW_MESSAGE_ON_PARCHE), any(), any(), eq("evt"));
    }
}
