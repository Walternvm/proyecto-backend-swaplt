package com.example.proyectobackendswaplt.notification;

import com.example.proyectobackendswaplt.auth.event.UserRegisteredEvent;
import com.example.proyectobackendswaplt.exchange.event.ExchangeCompletedEvent;
import com.example.proyectobackendswaplt.proposal.event.ProposalAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final MailService mailService;

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        mailService.send(event.user().getEmail(), "Bienvenido a Swaplt",
                "Hola " + event.user().getName() + ", tu cuenta fue creada exitosamente.");
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        String offererEmail = event.proposal().getUser().getEmail();
        String ownerEmail = event.proposal().getRequestedItem().getUser().getEmail();
        mailService.send(offererEmail, "Tu propuesta fue aceptada",
                "Tu propuesta de intercambio fue aceptada. Se generó el intercambio #" + event.exchange().getId() + ".");
        mailService.send(ownerEmail, "Aceptaste una propuesta",
                "Aceptaste una propuesta de intercambio. Se generó el intercambio #" + event.exchange().getId() + ".");
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExchangeCompleted(ExchangeCompletedEvent event) {
        String offeringEmail = event.exchange().getOfferingUser().getEmail();
        String receivingEmail = event.exchange().getReceivingUser().getEmail();
        String body = "El intercambio #" + event.exchange().getId() + " se completó exitosamente.";
        mailService.send(offeringEmail, "Intercambio completado", body);
        mailService.send(receivingEmail, "Intercambio completado", body);
    }
}
