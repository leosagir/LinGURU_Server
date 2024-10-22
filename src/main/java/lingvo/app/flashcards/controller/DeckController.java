package lingvo.app.flashcards.controller;

import lingvo.app.flashcards.dto.DeckCreateUpdateDto;
import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.service.DeckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/deck")
@RequiredArgsConstructor
@Slf4j
public class DeckController {

    private final DeckService deckService;

    @PostMapping
    public ResponseEntity<DeckResponseDto> createDeck(@RequestBody DeckCreateUpdateDto deckCreateUpdateDto) throws URISyntaxException {
       log.info("REST request to create Deck: {}", deckCreateUpdateDto);
    DeckResponseDto result = deckService.createDeck(deckCreateUpdateDto);
    return ResponseEntity.created(new URI("/api/deck/" + result.getId())).body(result);
    }

    @GetMapping
    public ResponseEntity<List<DeckResponseDto>> getAllDecks() {
        log.info("REST request to get all Decks");
        List<DeckResponseDto> decks = deckService.getAllDecks();
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponseDto> getDeck(@PathVariable Long id) {
        log.info("REST request to get Deck: {}", id);
        DeckResponseDto result = deckService.getDeckById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{word}")
    public ResponseEntity<DeckResponseDto> getDeckByTitle(@PathVariable String title) {
        log.info("REST request to get Deck by word: {}", title);
        DeckResponseDto result = deckService.getDeckByTitle(title);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponseDto> updateDeck(@PathVariable Long id, @RequestBody DeckCreateUpdateDto deckCreateUpdateDto) {
        log.info("REST request to update Deck: {}", id, deckCreateUpdateDto);
        DeckResponseDto result = deckService.updateDeck( id,deckCreateUpdateDto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        log.info("REST request to delete Deck: {}", id);
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
