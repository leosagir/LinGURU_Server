package lingvo.app.flashcards.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FlashcardUpdateDto {
    private String word;
    private String translationPerApi;
    private Set<String> selectedTranslations;
    private Set<String> usageExample;
    private Long deckId;
}
