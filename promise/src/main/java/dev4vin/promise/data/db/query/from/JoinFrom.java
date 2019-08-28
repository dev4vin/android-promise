package dev4vin.promise.data.db.query.from;

import dev4vin.promise.data.db.Column;
import dev4vin.promise.data.db.query.criteria.Criteria;
import dev4vin.promise.data.db.query.projection.Projection;
import dev4vin.promise.model.List;
import dev4vin.promise.model.function.MapFunction;

public class JoinFrom extends From {
  private From left;
  private From right;
  private String joinType;
  private Criteria criteria;

  public JoinFrom(From left, From right, String joinType, Criteria criteria) {
    this.left = left;
    this.right = right;
    this.joinType = joinType;
    this.criteria = criteria;
  }

  public JoinFrom onOr(Column leftColumn, Column rightColumn) {
    return onOr(Criteria.equals(Projection.column(leftColumn), Projection.column(rightColumn)));
  }

  public JoinFrom onAnd(Column leftColumn, Column rightColumn) {
    return onAnd(Criteria.equals(Projection.column(leftColumn), Projection.column(rightColumn)));
  }

  public JoinFrom onAnd(Criteria criteria) {
    this.criteria = (this.criteria != null ? this.criteria.and(criteria) : criteria);
    return this;
  }

  public JoinFrom onOr(Criteria criteria) {
    this.criteria = (this.criteria != null ? this.criteria.or(criteria) : criteria);
    return this;
  }

  @Override
  public String build() {
    String leftSide = (left != null ? left.build() : "");
    String rightSide = (right != null ? right.build() : "");
    String joinCriteria = (criteria != null ? criteria.build() : "");

    return "(" + leftSide + " " + joinType + " " + rightSide + " ON " + joinCriteria + ")";
  }

  @Override
  public List<String> buildParameters() {
    List<Object> ret = new List<Object>();

    if (left != null) ret.addAll(left.buildParameters());

    if (right != null) ret.addAll(right.buildParameters());

    if (criteria != null) ret.addAll(criteria.buildParameters());

    return ret.map(
        new MapFunction<String, Object>() {
          @Override
          public String from(Object o) {
            return String.valueOf(o);
          }
        });
  }
}
