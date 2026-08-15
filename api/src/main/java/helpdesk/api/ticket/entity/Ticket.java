package helpdesk.api.ticket.entity;

import helpdesk.api.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ABERTO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassificationOrigin classificationOrigin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id")
    private User responsible;

    @OneToMany(mappedBy = "ticket")
    private List<TicketComment> comments = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Ticket(
            String title,
            String description,
            TicketCategory category,
            TicketPriority priority,
            ClassificationOrigin classificationOrigin,
            User requester,
            User responsible
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.classificationOrigin = classificationOrigin;
        this.requester = requester;
        this.responsible = responsible;
    }
}
