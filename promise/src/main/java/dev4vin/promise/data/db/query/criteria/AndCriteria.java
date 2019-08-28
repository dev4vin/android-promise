package dev4vin.promise.data.db.query.criteria;

import dev4vin.promise.model.List;

public class AndCriteria extends Criteria {
  private Criteria left;
  private Criteria right;

  public AndCriteria(Criteria left, Criteria right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public String build() {
    String ret = " AND ";

    if (left != null) ret = left.build() + ret;

    if (right != null) ret = ret + right.build();

    return "(" + ret.trim() + ")";
  }

  @Override
  public List<String> buildParameters() {
    List<String> ret = new List<>();

    if (left != null) ret.addAll(left.buildParameters());

    if (right != null) ret.addAll(right.buildParameters());

    return ret;
  }
}
