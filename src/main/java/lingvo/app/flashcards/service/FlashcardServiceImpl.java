package lingvo.app.flashcards.service;

import lingvo.app.exception.ResourceNotFoundException;
import lingvo.app.flashcards.FlashcardMapper;
import lingvo.app.flashcards.dto.FlashcardCreateDto;
import lingvo.app.flashcards.dto.FlashcardResponseDto;
import lingvo.app.flashcards.dto.FlashcardUpdateDto;
import lingvo.app.flashcards.dto.FlashcardUpdateProgressDto;
import lingvo.app.flashcards.entity.Flashcard;
import lingvo.app.flashcards.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardMapper flashcardMapper;

    @Transactional
    @Override
    public FlashcardResponseDto createFlashcard(FlashcardCreateDto flashcardCreateDto) {
        log.info("Create Flashcard");

        Flashcard flashcard = flashcardMapper.flashcardCreateDtoToFlashcard(flashcardCreateDto);
        Flashcard flashcardSaved = flashcardRepository.save(flashcard);
        log.info("Flashcard created successfully", flashcardSaved);
        return flashcardMapper.flashcardToFlashcardResponseDto(flashcardSaved);
    }

    @Transactional
    @Override
    public List<FlashcardResponseDto> getAllFlashcards() {
        log.info("Get all flashcards");
        List<Flashcard> flashcards = flashcardRepository.findAll();
        return flashcards.stream()
                .map(flashcardMapper:: flashcardToFlashcardResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public FlashcardResponseDto getFlashcardById(Long id) {
        log.info("Get flashcard by id: {}", id);
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Flashcard not found"));
        return flashcardMapper.flashcardToFlashcardResponseDto(flashcard);
    }

    @Transactional
    @Override
    public FlashcardResponseDto getFlashcardByWord(String word) {
        log.info("Get flashcard by word: {}", word);
        Flashcard flashcard = flashcardRepository.findByWord(word)
                .orElseThrow(()->new ResourceNotFoundException("Flashcard not found"));
        return flashcardMapper.flashcardToFlashcardResponseDto(flashcard);
    }

    @Transactional
    @Override
    public FlashcardResponseDto updateFlashcard(Long id, FlashcardUpdateDto flashcardUpdateDto) {
        log.info("Update Flashcard");
        if (flashcardUpdateDto == null) {
            throw new IllegalArgumentException("FlashcardUpdateDto cannot be null");
        }
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Flashcard not found"));
        flashcardMapper.updateFlashcardFromDto(flashcardUpdateDto, flashcard);
        Flashcard flashcardUpdated = flashcardRepository.save(flashcard);
        log.info("Flashcard updated successfully", flashcardUpdated);
        return flashcardMapper.flashcardToFlashcardResponseDto(flashcardUpdated);
    }

    @Transactional
    @Override
    public void deleteFlashcard(Long id) {
        log.info("Delete Flashcard");
        if(!flashcardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flashcard not found");
        }
        flashcardRepository.deleteById(id);
        log.info("Flashcard deleted successfully");
    }

    @Transactional
    @Override
    public FlashcardResponseDto updateFlashcardProgress(Long id, FlashcardUpdateProgressDto flashcardUpdateProgressDto) {
        log.info("Update Flashcard progress");
        Flashcard flashcard = flashcardRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Flashcard not found"));
        flashcardMapper.updateFlashcardProgress(flashcardUpdateProgressDto, flashcard);
        Flashcard flashcardUpdated = flashcardRepository.save(flashcard);
        log.info("Flashcard progress updated successfully", flashcardUpdated);
        return flashcardMapper.flashcardToFlashcardResponseDto(flashcardUpdated);
    }

}
