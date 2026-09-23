package com.example.proyectobackendswaplt.publication.domain;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "publications")
@Getter @Setter @NoArgsConstructor
public class Publication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationStatus status = PublicationStatus.ACTIVE;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String wantedItem;

    @JsonIgnore
    @OneToMany(mappedBy = "publication")
    private List<Proposal> proposals = new ArrayList<>();
}
