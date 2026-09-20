package at.technikum.application.mrp.media;

import java.util.List;

public class Media {

    private int id;
    private String title;
    private String description;
    private String mediaType;
    private int releaseYear;
    private List<String> genres;
    private int ageRestriction;
    private String creatorUsername;

    public Media(
            int id,
            String title,
            String description,
            String mediaType,
            int releaseYear,
            List<String> genres,
            int ageRestriction,
            String creatorUsername
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mediaType = mediaType;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.ageRestriction = ageRestriction;
        this.creatorUsername = creatorUsername;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }
}
