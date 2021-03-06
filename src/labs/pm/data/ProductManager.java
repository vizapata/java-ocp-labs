package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
  private static final int REVIEW_EXTEND = 5;
  private static final Logger log = Logger.getLogger(ProductManager.class.getName());
  private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
  private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
  private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
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
    try {
      return reviewProduct(findProduct(id), rating, comments);
    } catch (ProjectManagerException ex) {
      log.info(ex.getMessage());
    }
    return null;
  }

  public Product reviewProduct(Product product, Rating rating, String comments) {
    List<Review> reviews = products.getOrDefault(product, new ArrayList<>());
    products.remove(product, reviews);
    reviews.add(new Review(rating, comments));

    double avg = reviews.stream()
            .mapToInt(r -> r.getRating().ordinal())
            .average()
            .orElse(0);

    product = product.applyRating(Rateable.convert((int) avg));

    products.put(product, reviews);
    return product;
  }

  public Product findProduct(int id) throws ProjectManagerException {
    return products.keySet()
            .stream()
            .filter(p -> p.getId() == id)
            .findFirst()
            .orElseThrow(() -> new ProjectManagerException(String.format("Product with id %s not found", id)));
  }

  public Map<String, String> getDiscounts() {
    return products.keySet().stream().collect(
            Collectors.groupingBy(
                    p -> p.getRating().getStars(),
                    Collectors.collectingAndThen(
                            Collectors.summingDouble(p -> p.getDiscount().doubleValue()),
                            d -> formatter.moneyFormat.format(d)
                    )));
  }

  public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
    String txt = products.keySet()
            .stream()
            .sorted(sorter)
            .filter(filter)
            .map(formatter::formatProduct)
            .collect(Collectors.joining(System.lineSeparator()));
    System.out.println(txt);
  }

  public void printProductReport(int id) {
    try {
      printProductReport(findProduct(id));
    } catch (ProjectManagerException ex) {
      log.info(ex.getMessage());
    }
  }

  public void printProductReport(Product product) {
    var report = new StringBuilder();
    report.append(formatter.formatProduct(product));
    report.append(System.lineSeparator());
    List<Review> reviews = products.getOrDefault(product, new ArrayList<>());
    Collections.sort(reviews);
    report.append(reviews.stream()
            .map(formatter::formatReview)
            .collect(Collectors.joining(System.lineSeparator())));
    if (products.get(product).isEmpty()) {
      report.append(formatter.getText("no.reviews"));
      report.append(System.lineSeparator());
    }
    System.out.println(report);
  }

  public void parseReview(String text) {
    try {
      Object[] values = reviewFormat.parse(text);
      reviewProduct(
              Integer.parseInt((String) values[0]),
              Rateable.convert(Integer.parseInt((String) values[1])),
              (String) values[2]
      );
    } catch (ParseException | NumberFormatException ex) {
      log.log(Level.WARNING, "Error parsing review: " + text);
    }
  }

  public void parseProduct(String text) {
    try {
      Object[] values = productFormat.parse(text);
      String type = (String) values[0];
      int id = Integer.parseInt((String) values[1]);
      String name = (String) values[2];
      BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[4]));
      Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
      switch (type) {
        case "D":
          createProduct(id, name, price, rating);
          break;
        case "F":
          LocalDate bestBefore = LocalDate.parse((String) values[5]);
          createProduct(id, name, price, rating, bestBefore);
          break;
        default:
          throw new ParseException("Invalid product type", 0);
      }
    } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
      log.log(Level.WARNING, "Error parsing product: " + text);
    }
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
