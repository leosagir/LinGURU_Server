package lingvo.app.flashcards.dto;

import lingvo.app.auth.entity.User;
import lingvo.app.flashcards.entity.Language;
import lombok.Data;

@Data
public class DeckCreateUpdateDto {

    private String title;

    private Language language;

}
