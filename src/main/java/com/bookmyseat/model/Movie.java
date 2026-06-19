package com.bookmyseat.model;

import java.time.LocalDate;

public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private int durationMins;
    private String language;
    private String rating;       // U, UA, A, S
    private LocalDate releaseDate;
    private String description;

    public Movie() {}

    public Movie(int movieId, String title, String genre, int durationMins,
                 String language, String rating, LocalDate releaseDate, String description) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.durationMins = durationMins;
        this.language = language;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.description = description;
    }

    public int getMovieId()             { return movieId; }
    public void setMovieId(int v)       { this.movieId = v; }
    public String getTitle()            { return title; }
    public void setTitle(String v)      { this.title = v; }
    public String getGenre()            { return genre; }
    public void setGenre(String v)      { this.genre = v; }
    public int getDurationMins()        { return durationMins; }
    public void setDurationMins(int v)  { this.durationMins = v; }
    public String getLanguage()         { return language; }
    public void setLanguage(String v)   { this.language = v; }
    public String getRating()           { return rating; }
    public void setRating(String v)     { this.rating = v; }
    public LocalDate getReleaseDate()   { return releaseDate; }
    public void setReleaseDate(LocalDate v){ this.releaseDate = v; }
    public String getDescription()      { return description; }
    public void setDescription(String v){ this.description = v; }

    /** Display duration as "2h 55m" */
    public String getDurationFormatted() {
        return durationMins / 60 + "h " + durationMins % 60 + "m";
    }

    @Override
    public String toString() {
        return "[" + movieId + "] " + title + " (" + language + " | " + rating +
                " | " + getDurationFormatted() + ")";
    }
}
