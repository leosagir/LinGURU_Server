package lingvo.app.flashcards.repository;

import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {
    Optional <Deck> findByTitle(String title);
}
