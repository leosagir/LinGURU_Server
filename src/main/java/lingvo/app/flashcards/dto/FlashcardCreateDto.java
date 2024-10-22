package lingvo.app.flashcards.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FlashcardCreateDto {
    private String word;
    private String translationPerApi;
    private Set<String> selectedTranslations;
    private Set<String> usageExample;
    private Long deckId;
}
