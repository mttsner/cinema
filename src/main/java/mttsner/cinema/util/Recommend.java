package mttsner.cinema.util;

import mttsner.cinema.movies.AgeRating;
import mttsner.cinema.movies.Genre;
import mttsner.cinema.movies.Language;
import mttsner.cinema.movies.Movie;

import java.util.HashMap;
import java.util.List;

public class Recommend {
    private final HashMap<Genre, Integer> genreWeights;
    private final HashMap<Language, Integer> languageWeights;
    private final HashMap<AgeRating, Integer> ageRatingWeights;
    private final HashMap<Integer, Integer> cache;

    public Recommend(List<Movie> movies) {
        genreWeights = new HashMap<>();
        languageWeights = new HashMap<>();
        ageRatingWeights = new HashMap<>();
        cache = new HashMap<>();
        // Update weights based on movie history
        for (Movie movie: movies) {
            updateWeights(movie);
        }
    }

    private void updateWeights(Movie movie) {
        // Update genre weights
        for (Genre genre: movie.getGenres()) {
            int gen = genreWeights.getOrDefault(genre, 0);
            genreWeights.put(genre, gen + 1);
        }
        // Update language weight
        int lang = languageWeights.getOrDefault(movie.getLanguage(), 0);
        languageWeights.put(movie.getLanguage(), lang + 1);
        // Update age rating weight
        int age = ageRatingWeights.getOrDefault(movie.getAgeRating(), 0);
        ageRatingWeights.put(movie.getAgeRating(), age + 1);
    }

    public int getScore(Movie movie) {
        // Memoize the score
        if (cache.containsKey(movie.getMovieId())) {
            return cache.get(movie.getMovieId());
        }
        int score = 0;
        // Increase score based on genre weights
        for (Genre genre : movie.getGenres()) {
            score += this.genreWeights.getOrDefault(genre, 0);
        }
        // Increase score based on language weights
        score += this.languageWeights.getOrDefault(movie.getLanguage(), 0);
        // Increase score based on age rating weights
        score += this.ageRatingWeights.getOrDefault(movie.getAgeRating(), 0);
        // Save score to cache
        cache.put(movie.getMovieId(), score);
        return score;
    }
}
