package lingvo.app.flashcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Table(name = "t_flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(name = "translation_per_api")
    private String translationPerApi;

    @ElementCollection
    @CollectionTable(name = "t_selected_translation", joinColumns = @JoinColumn(name = "flashcard_id"))
    @Column(name = "translation", length = 25)
    private Set<String> selectedTranslations;

    @ElementCollection
    @CollectionTable(name = "usage_example", joinColumns = @JoinColumn(name = "flashcard_id"))
    @Column(name = "example", length = 99)
    private Set<String> usageExample;

    @ManyToOne
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastReviewAt;

    @Column(nullable = false)
    private int reviewCount;

    @Min(0)
    @Max(10)
    @Column(nullable = false)
    private double difficultFactor;
}
