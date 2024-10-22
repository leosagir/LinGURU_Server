package lingvo.app.flashcards;

import lingvo.app.flashcards.dto.DeckCreateUpdateDto;
import lingvo.app.flashcards.dto.DeckResponseDto;
import lingvo.app.flashcards.entity.Deck;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeckMapper {

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdBy",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    Deck deckCreateUpdateDtoToDeck(DeckCreateUpdateDto deckCreateUpdateDto);

    DeckResponseDto deckToDeckResponseDto(Deck deck);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateDeckFromDto(DeckCreateUpdateDto dto, @MappingTarget Deck deck);
}
