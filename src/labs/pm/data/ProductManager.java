package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class ProductManager {
  private static final int REVIEW_EXTEND = 5;
  private Map<Product, List<Review>> products = new HashMap<>();
  private Review[] reviews = new Review[REVIEW_EXTEND];

  private Locale locale;
  private ResourceBundle resources;
  private DateTimeFormatter dateFormat;
  private NumberFormat moneyFormat;

  public ProductManager(Locale locale) {
    this.locale = locale;
    resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
    dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
    moneyFormat = NumberFormat.getCurrencyInstance(locale);
  }

  public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
    Product product = new Food(id, name, price, rating, bestBefore);
    products.putIfAbsent(product, new ArrayList<>());
    return product;
  }

  public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
    Product product = new Drink(id, name, price, rating);
    products.putIfAbsent(product, new ArrayList<>());
    return product;
  }


  public Product reviewProduct(int id, Rating rating, String comments) {
    return reviewProduct(findProduct(id), rating, comments);
  }

  public Product reviewProduct(Product product, Rating rating, String comments) {
    List<Review> reviews = products.getOrDefault(product, new ArrayList<>());
    products.remove(product, reviews);

    reviews.add(new Review(rating, comments));
    double avg = reviews.stream().mapToInt(r -> r.getRating().ordinal()).average().orElse(0);

    product = product.applyRating(Rateable.convert((int) avg));
    products.put(product, reviews);

    return product;
  }

  public Product findProduct(int id) {
    return products.keySet()
            .stream()
            .filter(product -> product.getId() == id)
            .findAny()
            .orElse(null);
  }

  public void printProductReport(int id) {
    Optional.ofNullable(findProduct(id)).ifPresentOrElse(this::printProductReport, () -> System.out.println("Inexistent product"));
  }

  public void printProductReport(Product product) {
    var report = new StringBuilder();
    report.append(MessageFormat.format(
            resources.getString("product"),
            product.getName(),
            moneyFormat.format(product.getPrice()),
            product.getRating().getStars(),
            dateFormat.format(product.getBestBefore())
    ));
    report.append(System.lineSeparator());
    List<Review> reviews = products.getOrDefault(product, new ArrayList<>());
    Collections.sort(reviews);
    for (Review review : reviews) {
      if (review == null) {
        break;
      }
      report.append(MessageFormat.format(resources.getString("review"), review.getRating().getStars(), review.getComments()));
      report.append(System.lineSeparator());
    }
//    Stream.of(reviews).forEach(r -> report.append(MessageFormat.format(resources.getString("review"), r.getRating().getStars(), r.getComments())));
    if (products.get(product).isEmpty()) {
      report.append(resources.getString("no.reviews"));
      report.append(System.lineSeparator());
    }

    System.out.println(report);
  }
}
