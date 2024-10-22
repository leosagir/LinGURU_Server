package lingvo.app.flashcards.entity;

public enum Language {
    ENGLISH("en"),
    GERMAN("de"),
    FRENCH("fr"),
    SPANISH("es"),
    ITALIAN("it"),
    RUSSIAN("ru"),
    UKRAINIAN("ua");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
