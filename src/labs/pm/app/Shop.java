package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;

public class Shop {

  public static void main(String[] args) {
    ProductManager pm = new ProductManager("en-US");

    pm.createProduct(101, "Tea", BigDecimal.valueOf(11.99), Rating.NOT_RATED);
    pm.reviewProduct(101, Rating.FOUR_STAR, "I don't like the tea pretty much");
    pm.reviewProduct(101, Rating.ONE_STAR, "Hate it");
    pm.reviewProduct(101, Rating.THREE_STAR, "So so");
    pm.reviewProduct(101, Rating.ONE_STAR, "Hate it");
    pm.reviewProduct(101, Rating.ONE_STAR, "Hate it");
    pm.reviewProduct(101, Rating.FIVE_STAR, "Love it");
    pm.printProductReport(101);

    pm.changeLocale("es-CO");

    pm.createProduct(102, "Coffee", BigDecimal.valueOf(2.99), Rating.NOT_RATED);
    pm.reviewProduct(102, Rating.FIVE_STAR, "I don't like the tea pretty much");
    pm.reviewProduct(102, Rating.FIVE_STAR, "Hate it");
    pm.reviewProduct(102, Rating.FIVE_STAR, "So so");
    pm.reviewProduct(102, Rating.FOUR_STAR, "Hate it");
    pm.printProductReport(102);

    pm.changeLocale("fr-FR");

    pm.createProduct(103, "Cake", BigDecimal.valueOf(2.99), Rating.NOT_RATED, LocalDate.now());
    pm.reviewProduct(103, Rating.FIVE_STAR, "Yummy");
    pm.reviewProduct(103, Rating.FOUR_STAR, "Hate it");
    pm.reviewProduct(103, Rating.FOUR_STAR, "So so");
    pm.reviewProduct(103, Rating.FOUR_STAR, "Hate it");
    pm.printProductReport(103);

    pm.getDiscounts().forEach((rating, discount) -> {
      System.out.println(rating + "\t" + discount);
    });

    Comparator<Product> ratingSorter = (p1, p2) -> p2.getRating().ordinal() - p1.getRating().ordinal();
    Comparator<Product> priceSorter = (p1, p2) -> p2.getPrice().compareTo(p1.getPrice());
    pm.printProducts(p -> p.getPrice().floatValue() > 2, ratingSorter.thenComparing(priceSorter).reversed());
  }
}
