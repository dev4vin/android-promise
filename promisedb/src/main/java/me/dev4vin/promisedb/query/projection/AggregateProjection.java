/*
 * Copyright 2017, Peter Vincent
 * Licensed under the Apache License, Version 2.0, Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.dev4vin.promisedb.query.projection;


import me.dev4vin.promisedb.Utils;
import me.dev4vin.promisemodel.List;
import me.dev4vin.promisemodel.function.MapFunction;

public class AggregateProjection extends Projection {
  private Projection projection;
  private int type;
  public AggregateProjection(Projection projection, int type) {
    this.projection = projection;
    this.type = type;
  }

  @Override
  public String build() {
    String ret = (projection != null ? projection.build() : "");

    if (type == Type.MIN) return "MIN(" + ret + ")";
    else if (type == Type.MAX) return "MAX(" + ret + ")";
    else if (type == Type.SUM) return "SUM(" + ret + ")";
    else if (type == Type.AVG) return "AVG(" + ret + ")";
    else if (type == Type.COUNT) return "COUNT(" + ret + ")";
    else return ret;
  }

  @Override
  public List<String> buildParameters() {
    if (projection != null) return projection.buildParameters();
    else
      return Utils.EMPTY_LIST.map(
          new MapFunction<String, Object>() {
            @Override
            public String from(Object o) {
              return String.valueOf(o);
            }
          });
  }

  public static class Type {
    public static final int MIN = 1;
    public static final int MAX = 2;
    public static final int SUM = 3;
    public static final int AVG = 4;
    public static final int COUNT = 5;
  }
}
