package lingvo.app.flashcards.repository;

import lingvo.app.flashcards.entity.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
Optional<Flashcard> findByWord(String word);
}
