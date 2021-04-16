package labs.pm.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Product implements Rateable<Product> {
  public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);
  private final int id;
  private final String name;
  private final BigDecimal price;
  private final Rating rating;

  Product(int id, String name, BigDecimal price, Rating rating) {
    super();
    this.id = id;
    this.name = name;
    this.price = price;
    this.rating = rating;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getDiscount() {
    return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public Rating getRating() {
    return rating;
  }

  public LocalDate getBestBefore() {
    return LocalDate.now();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", price=" + price +
            ", bestBefore=" + getBestBefore() +
            ", discount=" + getDiscount() +
            ", rating=" + rating.getStars() +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
    if (o instanceof Product) {
      final Product product = (Product) o;
      return id == product.id;//  && Objects.equals(name, product.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
