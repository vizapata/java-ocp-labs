package labs.pm.data;

@FunctionalInterface
public interface Rateable<T> {
  Rating DEFAULT_RATING = Rating.NOT_RATED;

  static Rating convert(int stars) {
    return (stars >= 0 && stars <= 5) ? Rating.values()[stars] : DEFAULT_RATING;
  }

  default T applyRating(int stars) {
    return applyRating(convert(stars));
  }

  T applyRating(Rating rating);

  default Rating getRating() {
    return DEFAULT_RATING;
  }
}
