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
  private final Map<String, ResourceFormatter> formatters = Map.of(
          "en-US", new ResourceFormatter(Locale.US),
          "es-CO", new ResourceFormatter(new Locale("es", "CO"))
  );
  private final Map<Product, List<Review>> products = new HashMap<>();
  private final Review[] reviews = new Review[REVIEW_EXTEND];
  private ResourceFormatter formatter;

  public ProductManager(String languageTag) {
    changeLocale(languageTag);
  }

  public ProductManager(Locale locale) {
    this(locale.toLanguageTag());
  }

  public void changeLocale(String languageTag) {
    formatter = formatters.getOrDefault(languageTag, formatters.get("en-US"));
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

  public void printProducts(Comparator<Product> sorter){
    List<Product> productList = new ArrayList<>(products.keySet());
    productList.sort(sorter);
    productList.forEach(p -> {
      System.out.println(formatter.formatProduct(p));
    });
  }

  public void printProductReport(int id) {
    Optional.ofNullable(findProduct(id)).ifPresentOrElse(this::printProductReport, () -> System.out.println("Inexistent product"));
  }

  public void printProductReport(Product product) {
    var report = new StringBuilder();
    report.append(formatter.formatProduct(product));
    report.append(System.lineSeparator());
    List<Review> reviews = products.getOrDefault(product, new ArrayList<>());
    Collections.sort(reviews);
    for (Review review : reviews) {
      if (review == null) {
        break;
      }
      report.append(formatter.formatReview(review));
      report.append(System.lineSeparator());
    }
    if (products.get(product).isEmpty()) {
      report.append(formatter.getText("no.reviews"));
      report.append(System.lineSeparator());
    }

    System.out.println(report);
  }

  private static class ResourceFormatter {
    private Locale locale;
    private ResourceBundle resources;
    private DateTimeFormatter dateFormat;
    private NumberFormat moneyFormat;

    public ResourceFormatter(Locale locale) {
      this.locale = locale;
      resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
      dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
      moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }

    private String formatProduct(Product product) {
      return MessageFormat.format(
              getText("product"),
              product.getName(),
              moneyFormat.format(product.getPrice()),
              product.getRating().getStars(),
              dateFormat.format(product.getBestBefore())
      );
    }

    private String formatReview(Review review) {
      return MessageFormat.format(getText("review"), review.getRating().getStars(), review.getComments());
    }

    private String getText(String key) {
      return resources.getString(key);
    }
  }
}
