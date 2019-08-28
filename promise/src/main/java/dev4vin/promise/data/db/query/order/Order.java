package dev4vin.promise.data.db.query.order;

import dev4vin.promise.data.db.Column;
import dev4vin.promise.data.db.query.projection.AliasedProjection;
import dev4vin.promise.data.db.query.projection.Projection;
import dev4vin.promise.model.List;

public abstract class Order {
  protected Projection projection;

  public Order(Projection projection) {
    this.projection = projection;

    if (this.projection instanceof AliasedProjection)
      this.projection = ((AliasedProjection) this.projection).removeAlias();
  }

  public static Order orderByAscending(Column column) {
    return new OrderAscending(Projection.column(column));
  }

  public static Order orderByDescending(Column column) {
    return new OrderDescending(Projection.column(column));
  }

  public static Order orderByAscending(Projection projection) {
    return new OrderAscending(projection);
  }

  public static Order orderByDescending(Projection projection) {
    return new OrderDescending(projection);
  }

  public static Order orderByAscendingIgnoreCase(Column column) {
    return new OrderAscendingIgnoreCase(Projection.column(column));
  }

  public static Order orderByDescendingIgnoreCase(Column column) {
    return new OrderDescendingIgnoreCase(Projection.column(column));
  }

  public static Order orderByAscendingIgnoreCase(Projection projection) {
    return new OrderAscendingIgnoreCase(projection);
  }

  public static Order orderByDescendingIgnoreCase(Projection projection) {
    return new OrderDescendingIgnoreCase(projection);
  }

  public abstract String build();

  public abstract List<String> buildParameters();
}
