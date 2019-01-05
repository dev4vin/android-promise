package me.yoctopus.data.db.query.criteria;

import me.yoctopus.data.db.query.projection.AliasedProjection;
import me.yoctopus.data.db.query.projection.Projection;
import me.yoctopus.model.List;

public class NotInCriteria extends Criteria {
  private Projection projection;
  private List<Object> valuesList;
  private Object[] valuesArray;

  public NotInCriteria(Projection projection, List<Object> values) {
    this.projection = projection;
    this.valuesList = values;
    this.valuesArray = null;

    if (this.projection instanceof AliasedProjection)
      this.projection = ((AliasedProjection) this.projection).removeAlias();
  }

  public NotInCriteria(Projection projection, Object[] values) {
    this.projection = projection;
    this.valuesArray = values;
    this.valuesList = null;

    if (this.projection instanceof AliasedProjection)
      this.projection = ((AliasedProjection) this.projection).removeAlias();
  }

  @Override
  public String build() {
    StringBuilder sb = new StringBuilder();

    if (projection != null) sb.append(projection.build());

    sb.append(" NOT IN (");

    if (valuesList != null) {
      if (valuesList.size() <= 0) return "1=1";

      for (int i = 0; i < valuesList.size(); i++) {
        if (valuesList.get(i) != null) sb.append("?, ");
        else sb.append("NULL, ");
      }
    } else {
      if (valuesArray.length <= 0) return "1=1";

      for (int i = 0; i < valuesArray.length; i++) {
        if (valuesArray[i] != null) sb.append("?, ");
        else sb.append("NULL, ");
      }
    }

    sb.setLength(sb.length() - 2); // removes the ", " from the last entry
    sb.append(")");

    return sb.toString();
  }

  @Override
  public List<String> buildParameters() {
    List<String> ret = new List<>();

    if (projection != null) ret.addAll(projection.buildParameters());
    if (valuesList != null)
      for (int i = 0; i < valuesList.size(); i++) {
        if (valuesList.get(i) != null) ret.add(String.valueOf(valuesList.get(i)));
      }
    else
      for (Object aValuesArray : valuesArray) {
        if (aValuesArray != null) ret.add(String.valueOf(aValuesArray));
      }

    return ret;
  }
}
