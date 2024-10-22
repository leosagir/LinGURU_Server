package lingvo.app.flashcards.dto;

import jakarta.persistence.*;
import lingvo.app.auth.entity.User;
import lingvo.app.flashcards.entity.Language;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class DeckResponseDto {

    private Long id;

    private String title;

    private Language language;

    private User createdBy;

    private LocalDateTime createdAt;
}
