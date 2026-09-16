package com.example.proyectobackendswaplt.review.domain;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
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
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exchange_id")
    private Exchange exchange;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private Integer rating;

    private String comment;
}
