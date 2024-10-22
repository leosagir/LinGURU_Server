package lingvo.app.flashcards.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlashcardUpdateProgressDto {

    private Long id;

    private LocalDateTime lastReviewAt;

    private int reviewCount;

    private double difficultFactor;
}
