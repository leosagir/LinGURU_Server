package lingvo.app.flashcards;

import org.mapstruct.*;
import lingvo.app.flashcards.dto.*;
import lingvo.app.flashcards.entity.Flashcard;
import lingvo.app.flashcards.entity.Deck;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlashcardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastReviewAt", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "difficultFactor", ignore = true)
    @Mapping(target = "deck", source = "deckId", qualifiedByName = "deckIdToDeck")
    Flashcard flashcardCreateDtoToFlashcard(FlashcardCreateDto flashcardCreateDto);

    @Mapping(target = "deck", source = "deckId", qualifiedByName = "deckIdToDeck")
    void updateFlashcardFromDto(FlashcardUpdateDto flashcardUpdateDto, @MappingTarget Flashcard flashcard);

    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "language", source = "deck.language")
    FlashcardResponseDto flashcardToFlashcardResponseDto(Flashcard flashcard);

    List<FlashcardResponseDto> flashcardsToFlashcardResponseDto(List<Flashcard> flashcards);

    @Mapping(target = "word", ignore = true)
    @Mapping(target = "translationPerApi", ignore = true)
    @Mapping(target = "selectedTranslations", ignore = true)
    @Mapping(target = "usageExample", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFlashcardProgress(FlashcardUpdateProgressDto flashcardUpdateProgressDto, @MappingTarget Flashcard flashcard);

    @Named("deckIdToDeck")
    default Deck deckIdToDeck(Long id) {
        if (id == null) {
            return null;
        }
        Deck deck = new Deck();
        deck.setId(id);
        return deck;
    }

    @Mapping(target = "word", ignore = true)
    @Mapping(target = "translationPerApi", ignore = true)
    @Mapping(target = "selectedTranslations", ignore = true)
    @Mapping(target = "usageExample", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastReviewAt", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "difficultFactor", ignore = true)
    Flashcard flashcardDeleteDtoToFlashcard(FlashcardDeleteDto flashcardDeleteDto);
}