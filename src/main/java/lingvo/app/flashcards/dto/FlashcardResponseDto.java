package lingvo.app.flashcards.dto;

import lingvo.app.flashcards.entity.Language;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class FlashcardResponseDto {
    private Long id;
    private String word;
    private String translationPerApi;
    private Set<String> selectedTranslations;
    private Language language;
    private Set<String> usageExample;
    private Long deckId;
    private LocalDateTime createdAt;
    private LocalDateTime lastReviewAt;
    private int reviewCount;
    private double difficultFactor;
}
