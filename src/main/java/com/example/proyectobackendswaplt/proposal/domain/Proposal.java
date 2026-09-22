package com.example.proyectobackendswaplt.proposal.domain;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "proposals")
public class Proposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_item_id", nullable = false)
    private Item offeredItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_item_id", nullable = false)
    private Item requestedItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id")
    private Publication publication;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status = ProposalStatus.PENDING;

    private String message;
}
