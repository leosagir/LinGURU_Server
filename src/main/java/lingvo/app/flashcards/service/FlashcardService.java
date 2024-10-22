package lingvo.app.flashcards.service;

import lingvo.app.flashcards.dto.FlashcardCreateDto;
import lingvo.app.flashcards.dto.FlashcardResponseDto;
import lingvo.app.flashcards.dto.FlashcardUpdateDto;
import lingvo.app.flashcards.dto.FlashcardUpdateProgressDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FlashcardService {
    @Transactional
    FlashcardResponseDto createFlashcard(FlashcardCreateDto flashcardCreateDto);

    @Transactional
    List<FlashcardResponseDto> getAllFlashcards();

    @Transactional
    FlashcardResponseDto getFlashcardById(Long id);

    @Transactional
    FlashcardResponseDto getFlashcardByWord(String word);

    @Transactional
    FlashcardResponseDto updateFlashcard(Long id, FlashcardUpdateDto flashcardUpdateDto);

    @Transactional
    void deleteFlashcard(Long id);

    @Transactional
    FlashcardResponseDto updateFlashcardProgress(Long id, FlashcardUpdateProgressDto flashcardUpdateProgressDto);
}
