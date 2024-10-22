package lingvo.app.flashcards.service;

import lingvo.app.exception.ResourceNotFoundException;
import lingvo.app.flashcards.dto.DeckCreateUpdateDto;
import lingvo.app.flashcards.DeckMapper;
import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.entity.Deck;
import lingvo.app.flashcards.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeckServiceImpl implements DeckService {

    private final DeckRepository deckRepository;
    private final DeckMapper deckMapper;

    @Transactional
    @Override
    public DeckResponseDto createDeck(DeckCreateUpdateDto deckCreateUpdateDto) {
        log.info("Create deck with title: {}", deckCreateUpdateDto.getTitle());

        Deck deck = deckMapper.deckCreateUpdateDtoToDeck(deckCreateUpdateDto);
        Deck savedDeck = deckRepository.save(deck);
        log.info("Deck created successfully with id: {}", savedDeck.getId());
        return deckMapper.deckToDeckResponseDto(savedDeck);
    }

    @Transactional
    @Override
    public DeckResponseDto getDeckById(long deckId){
        log.info("Fetching deck with id: {}", deckId);
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(()->new ResourceNotFoundException("Deck not found"));
        return deckMapper.deckToDeckResponseDto(deck);
    }

    @Transactional
    @Override
    public List<DeckResponseDto> getAllDecks(){
        log.info("Fetching all decks");
        List<Deck> decks = deckRepository.findAll();
        return decks.stream()
                .map(deckMapper::deckToDeckResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DeckResponseDto getDeckByTitle(String deckTitle){
        log.info("Fetching deck by title: {}", deckTitle);
        Deck deck = deckRepository.findByTitle(deckTitle)
                .orElseThrow(()->new ResourceNotFoundException("Deck not found"));
        return deckMapper.deckToDeckResponseDto(deck);
    }

    @Transactional
    @Override
    public DeckResponseDto updateDeck(Long id, DeckCreateUpdateDto deckCreateUpdateDto) {
        log.info("Updating deck with id: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("Deck id cannot be null");
        }
        if (deckCreateUpdateDto == null) {
            throw new IllegalArgumentException("DeckCreateUpdateDto cannot be null");
        }
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found with id: " + id));
        deckMapper.updateDeckFromDto(deckCreateUpdateDto, deck);
        Deck updatedDeck = deckRepository.save(deck);
        log.info("Deck with id: {} updated successfully", updatedDeck.getId());
        return deckMapper.deckToDeckResponseDto(updatedDeck);
    }

    @Transactional
    @Override
    public void deleteDeck(long deckId){
        log.info("Deleting deck with id: {}", deckId);
        if(!deckRepository.existsById(deckId)){
            throw new ResourceNotFoundException("Deck not found");
        }
        deckRepository.deleteById(deckId);
        log.info("Deck deleted successfully with id: {}", deckId);
    }

}
