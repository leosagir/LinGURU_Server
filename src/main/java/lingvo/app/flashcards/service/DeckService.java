package lingvo.app.flashcards.service;

import lingvo.app.flashcards.dto.DeckCreateUpdateDto;
import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.entity.Deck;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DeckService {
    @Transactional
    DeckResponseDto createDeck(DeckCreateUpdateDto deckCreateUpdateDto);

    @Transactional
    DeckResponseDto getDeckById(long deckId);

    @Transactional
    List<DeckResponseDto> getAllDecks();

    @Transactional
    DeckResponseDto getDeckByTitle(String deckTitle);

    @Transactional
    DeckResponseDto updateDeck(Long id, DeckCreateUpdateDto deckCreateUpdateDto);

    @Transactional
    void deleteDeck(long deckId);
}
