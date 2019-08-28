package dev4vin.promise.data.db.query.projection;

import dev4vin.promise.data.db.Column;
import dev4vin.promise.data.db.Table;
import dev4vin.promise.data.db.query.QueryBuilder;
import dev4vin.promise.model.List;

public abstract class Projection {
  // Simple column
  public static ColumnProjection column(Column column) {
    return new ColumnProjection(null, column);
  }

  public static ColumnProjection column(Table table, Column column) {
    return new ColumnProjection(table, column);
  }

  // Constant
  public static ConstantProjection constant(Object constant) {
    return new ConstantProjection(constant);
  }

  // Aggregate functions
  public static AggregateProjection min(Column<Integer> column) {
    return min(column(column));
  }

  public static AggregateProjection max(Column<Integer> column) {
    return max(column(column));
  }

  public static AggregateProjection sum(Column<Integer> column) {
    return sum(column(column));
  }

  public static AggregateProjection avg(Column<Integer> column) {
    return avg(column(column));
  }

  public static AggregateProjection count(Column column) {
    return count(column(column));
  }

  public static AggregateProjection countRows() {
    return count(column(new Column("*", Column.Type.TEXT.NULLABLE())));
  }

  public static AggregateProjection min(Projection projection) {
    return new AggregateProjection(projection, AggregateProjection.Type.MIN);
  }

  public static AggregateProjection max(Projection projection) {
    return new AggregateProjection(projection, AggregateProjection.Type.MAX);
  }

  public static AggregateProjection sum(Projection projection) {
    return new AggregateProjection(projection, AggregateProjection.Type.SUM);
  }

  public static AggregateProjection avg(Projection projection) {
    return new AggregateProjection(projection, AggregateProjection.Type.AVG);
  }

  public static AggregateProjection count(Projection projection) {
    return new AggregateProjection(projection, AggregateProjection.Type.COUNT);
  }

  // SubQuery
  public static SubQueryProjection subQuery(QueryBuilder subQuery) {
    return new SubQueryProjection(subQuery);
  }

  public Projection as(String alias) {
    return new AliasedProjection(this, alias);
  }

  public Projection castAsDate() {
    return new CastDateProjection(this);
  }

  public Projection castAsDateTime() {
    return new CastDateTimeProjection(this);
  }

  public Projection castAsReal() {
    return new CastRealProjection(this);
  }

  public Projection castAsInt() {
    return new CastIntProjection(this);
  }

  public Projection castAsString() {
    return new CastStringProjection(this);
  }

  public abstract String build();

  public abstract List<String> buildParameters();
}
