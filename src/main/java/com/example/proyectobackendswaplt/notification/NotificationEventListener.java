package com.example.proyectobackendswaplt.notification;

import com.example.proyectobackendswaplt.auth.event.UserRegisteredEvent;
import com.example.proyectobackendswaplt.exchange.event.ExchangeCompletedEvent;
import com.example.proyectobackendswaplt.proposal.event.ProposalAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final MailService mailService;

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        mailService.send(
                event.getUserEmail(),
                "Bienvenido a Swaplt",
                "Hola "
                        + event.getUserName()
                        + ", tu cuenta fue creada exitosamente."
        );
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        String body =
                "Se genero el intercambio #"
                        + event.getExchangeId()
                        + ".";

        mailService.send(
                event.getOfferingUserEmail(),
                "Tu propuesta fue aceptada",
                "Tu propuesta de intercambio fue aceptada. "
                        + body
        );

        mailService.send(
                event.getReceivingUserEmail(),
                "Aceptaste una propuesta",
                "Aceptaste una propuesta de intercambio. "
                        + body
        );
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onExchangeCompleted(ExchangeCompletedEvent event) {
        String body =
                "El intercambio #"
                        + event.getExchangeId()
                        + " se completo exitosamente.";

        mailService.send(
                event.getOfferingUserEmail(),
                "Intercambio completado",
                body
        );

        mailService.send(
                event.getReceivingUserEmail(),
                "Intercambio completado",
                body
        );
    }
}
