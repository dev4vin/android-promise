package dev4vin.promise.data.db.query.from;

import dev4vin.promise.data.db.Utils;
import dev4vin.promise.data.db.query.QueryBuilder;
import dev4vin.promise.model.List;
import dev4vin.promise.model.function.MapFunction;

public class SubQueryFrom extends AliasableFrom<SubQueryFrom> {
  private QueryBuilder subQuery;

  public SubQueryFrom(QueryBuilder subQuery) {
    this.subQuery = subQuery;
  }

  @Override
  public String build() {
    String ret = (subQuery != null ? "(" + subQuery.build() + ")" : "");

    if (!Utils.isNullOrWhiteSpace(alias)) ret = ret + " AS " + alias;

    return ret;
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
