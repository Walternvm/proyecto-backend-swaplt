package com.example.proyectobackendswaplt.proposal.domain;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.user.domain.User;
import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "offered_item_id")
    private Item offeredItem;

    @ManyToOne
    @JoinColumn(name = "requested_item_id")
    private Item requestedItem;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;

    private String message;
}
