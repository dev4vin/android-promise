package dev4vin.promise.data.db.query.projection;

import dev4vin.promise.data.db.Utils;
import dev4vin.promise.data.db.query.QueryBuilder;
import dev4vin.promise.model.List;
import dev4vin.promise.model.function.MapFunction;

public class SubQueryProjection extends Projection {
  private QueryBuilder subQuery;

  public SubQueryProjection(QueryBuilder subQuery) {
    this.subQuery = subQuery;
  }

  @Override
  public String build() {
    if (subQuery != null) return "(" + subQuery.build() + ")";
    else return "";
  }

  @Override
  public List<String> buildParameters() {
    if (subQuery != null) return List.fromArray(subQuery.buildParameters());
    else
      return Utils.EMPTY_LIST.map(
          new MapFunction<String, Object>() {
            @Override
            public String from(Object o) {
              return String.valueOf(o);
            }
          });
  }
}
