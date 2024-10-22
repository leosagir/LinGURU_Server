package lingvo.app.flashcards.controller;

import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.dto.FlashcardCreateDto;
import lingvo.app.flashcards.dto.FlashcardResponseDto;
import lingvo.app.flashcards.dto.FlashcardUpdateDto;
import lingvo.app.flashcards.entity.Flashcard;
import lingvo.app.flashcards.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/flashcard")
@RequiredArgsConstructor
@Slf4j
public class FlashcardController {
    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<FlashcardResponseDto> createFlashcard(@RequestBody FlashcardCreateDto flashcardCreateDto) throws URISyntaxException {
        log.info("create Flashcard", flashcardCreateDto);
        FlashcardResponseDto result = flashcardService.createFlashcard(flashcardCreateDto);
        return ResponseEntity.created(new URI("/api/flashcard/" + result.getId())).body(result);
    }

    @GetMapping
    public ResponseEntity<List<FlashcardResponseDto>> getAllFlashcards() {
        log.info("Rest request to get all flashcards");
        List<FlashcardResponseDto> result = flashcardService.getAllFlashcards();
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/word/{word}")
    public ResponseEntity<FlashcardResponseDto> getFlashcardByWord(@PathVariable String word) {
        log.info("Rest request to get flashcard by word {}", word);
        FlashcardResponseDto result = flashcardService.getFlashcardByWord(word);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashcardResponseDto> getFlashcardById(@PathVariable Long id) {
        log.info("Rest request to get flashcard by id {}", id);
        FlashcardResponseDto result = flashcardService.getFlashcardById(id);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashcardResponseDto> updateFlashcard(@PathVariable Long id, @RequestBody FlashcardUpdateDto flashcardUpdateDto) {
        log.info("update Flashcard {}", flashcardUpdateDto);
        FlashcardResponseDto result = flashcardService.updateFlashcard(id, flashcardUpdateDto);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable Long id) {
        log.info("delete Flashcard {}", id);
        flashcardService.deleteFlashcard(id);
        return ResponseEntity.noContent().build();
    }
}