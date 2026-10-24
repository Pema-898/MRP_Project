package at.technikum.application.mrp.media;

import at.technikum.application.common.exception.BadRequestException;

public enum MediaType {
    MOVIE("movie"),
    SERIES("series"),
    GAME("game");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MediaType from(String value) {
        for (MediaType type : values()) {
            if (type.value.equalsIgnoreCase(value)) return type;
        }
        throw new BadRequestException("MEDIA_TYPE_INVALID");
    }
}
