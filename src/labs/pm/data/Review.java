package labs.pm.data;

public class Review implements Comparable<Review> {
  private Rating rating;
  private String comments;

  public Review(Rating rating, String comments) {
    this.rating = rating;
    this.comments = comments;
  }

  public Rating getRating() {
    return rating;
  }

  public String getComments() {
    return comments;
  }

  @Override
  public String toString() {
    return String.format(
            "%s{rating=%s, comments=\"%s\"}",
            getClass().getSimpleName(),
            rating,
            comments);
  }

  @Override
  public int compareTo(Review other) {
    if (other == null) return 1;
    return other.getRating().ordinal() - getRating().ordinal();
  }
}
