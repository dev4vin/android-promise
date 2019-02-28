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

package me.dev4vin.promisedb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import java.util.Collections;

import androidx.annotation.Nullable;
import me.dev4vin.LogUtil;
import me.dev4vin.promisedb.query.QueryBuilder;
import me.dev4vin.promisedb.query.criteria.Criteria;
import me.dev4vin.promisemodel.List;
import me.dev4vin.promisemodel.function.Converter;
import me.dev4vin.utils.Conditions;


public abstract class Model<T extends SModel>
    implements Table<T, SQLiteDatabase>, Converter<T, Cursor, ContentValues> {
  private static final String CREATE_PREFIX = "CREATE TABLE IF NOT EXISTS ";
  private static final String DROP_PREFIX = "TRUNCATE TABLE IF EXISTS ";
  private static final String SELECT_PREFIX = "SELECT * FROM ";
  public static Column<Integer> id;

  static {
    id = new Column<>("id", Column.Type.INTEGER.PRIMARY_KEY_AUTOINCREMENT());
  }

  private String ALTER_COMMAND = "ALTER TABLE";
  private String name = "`" + getName() + "`";
  private String TAG = LogUtil.makeTag(Model.class).concat(name);
  private SList<T> backup;

  public abstract List<Column> getColumns();

  public int getNumberOfColumns() {
    return getColumns().size() + 1;
  }

  @Override
  public boolean onCreate(SQLiteDatabase database) throws ModelError {
    String sql = CREATE_PREFIX;
    sql = sql.concat(name + "(");
    List<Column> columns = getColumns();
    columns = Conditions.checkNotNull(columns);
    Collections.sort(columns, Column.ascending);
    List<Column> columns1 = new List<>();
    columns1.add(id);
    columns1.addAll(columns);
    for (int i = 0; i < columns1.size(); i++) {
      Column column = columns1.get(i);
      if (i == columns1.size() - 1) sql = sql.concat(column.toString());
      else sql = sql.concat(column.toString() + ", ");
    }
    sql = sql.concat(");");
    try {
      LogUtil.d(TAG, sql);
      database.execSQL(sql);
    } catch (SQLException e) {
      throw new ModelError(e);
    }
    return true;
  }

  @Override
  public boolean onUpgrade(SQLiteDatabase database, int v1, int v2) throws ModelError {
    /*synchronized (this) {
        backup(database);
        if (onDrop(database)) onCreate(database);
        restore(database);
    }*/
    return false;
  }

  public boolean addColumns(SQLiteDatabase database, Column... columns) throws ModelError {
    for (Column column : columns) {
      String alterSql = ALTER_COMMAND + " `" + getName() + "` " + "ADD " + column.toString() + ";";
      try {
        LogUtil.d(TAG, alterSql);
        database.execSQL(alterSql);
      } catch (SQLException e) {
        throw new ModelError(e);
      }
    }
    return true;
  }

  public boolean dropColumns(SQLiteDatabase database, Column... columns) throws ModelError {
    for (Column column : columns) {
      String alterSql =
          ALTER_COMMAND + " `" + getName() + "` " + "DROP COLUMN " + column.getName() + ";";
      try {
        database.execSQL(alterSql);
      } catch (SQLException e) {
        throw new ModelError(e);
      }
    }
    return true;
  }

  @Override
  public Extras<T> read(final SQLiteDatabase database) {
    return new QueryExtras<T>(database) {
      @Override
      public ContentValues get(T t) {
        return Model.this.get(t);
      }

      @Override
      public T from(Cursor cursor) {
        return Model.this.from(cursor);
      }
    };
  }

  @Override
  public Extras<T> read(SQLiteDatabase database, Column... columns) {
    return new QueryExtras2<T>(database, columns) {
      @Override
      public ContentValues get(T t) {
        return Model.this.get(t);
      }

      @Override
      public T from(Cursor cursor) {
        return Model.this.from(cursor);
      }
    };
  }

  @Override
  public final SList<T> onReadAll(SQLiteDatabase database, boolean close) {
    QueryBuilder builder = new QueryBuilder().from(this).takeAll();
    Cursor cursor;
    LogUtil.d(TAG, "query: ", builder.toDebugSqlString());
    cursor = database.rawQuery(builder.build(), builder.buildParameters());
    SList<T> ts = new SList<>();
    while (cursor.moveToNext() && !cursor.isClosed()) ts.add(getWithId(cursor));
    cursor.close();
    /*if (close) database.close();*/
    return ts;
  }

  @Override
  public SList<T> onReadAll(SQLiteDatabase database, Column column) {
    if (column == null) return onReadAll(database, true);
    String where = column.getName() + column.getOperand();
    if (column.value() instanceof String) where = where + "\"" + column.value() + "\"";
    else where = where + column.value();
    Cursor cursor = database.query(name, null, where, null, null, null, null);
    SList<T> ts = new SList<>();
    while (cursor.moveToNext() && !cursor.isClosed()) ts.add(getWithId(cursor));
    cursor.close();
    /*database.close();*/
    return ts;
  }

  @Override
  public SList<T> onReadAll(SQLiteDatabase database, Column[] columns) {
    if (columns == null) return onReadAll(database, true);
    String[] args = new String[columns.length];
    String selection = "";
    for (int i = 0; i < args.length; i++) {
      Column column = columns[i];
      String where = column.getName() + column.getOperand();
      selection = selection.concat(where);
      if (column.value() instanceof String) args[i] = "\"" + column.value() + "\"";
      else args[i] = column.value().toString();
      selection = selection.concat(args[i]);
      if (i < args.length - 1) selection = selection.concat(" AND ");
    }
    Cursor cursor = database.query(name, null, selection, null, null, null, null);
    SList<T> ts = new SList<>();
    while (cursor.moveToNext() && !cursor.isClosed()) ts.add(getWithId(cursor));
    cursor.close();
    /*database.close();*/
    return ts;
  }

  @Override
  public final boolean onUpdate(T t, SQLiteDatabase database, Column column) {
    String where = null;
    if (column != null) where = column.getName() + column.getOperand() + column.value();
    return database.update(name, get(t), where, null) >= 0;
  }

  @Override
  public boolean onUpdate(T t, SQLiteDatabase database) {
    return id != null && onUpdate(t, database, id.with(t.id()));
  }

  @Override
  public final <C> boolean onDelete(SQLiteDatabase database, Column<C> column, List<C> list) {
    boolean deleted = true;
    String where;
    for (int i = 0, listSize = list.size(); i < listSize; i++) {
      C c = list.get(i);
      where = column.getName() + column.getOperand() + c;
      deleted = database.delete(name, where, null) >= 0;
    }
    return deleted;
  }

  @Override
  public final boolean onDelete(SQLiteDatabase database, Column column) {
    if (column == null) return false;
    String where = column.getName() + column.getOperand() + column.value();
    return database.delete(name, where, null) >= 0;
  }

  @Override
  public boolean onDelete(T t, SQLiteDatabase database) {
    return onDelete(database, id.with(t.id()));
  }

  @Override
  public final boolean onDelete(SQLiteDatabase database) {
    return !TextUtils.isEmpty(name) && database.delete(name, null, null) >= 0;
  }

  @Override
  public final long onSave(T t, SQLiteDatabase database) {
    /*database.close();*/
    return database.insert(name, null, get(t));
  }

  @Override
  public final boolean onSave(SList<T> list, SQLiteDatabase database, boolean close) {
    boolean saved = true;
    int i = 0, listSize = list.size();
    while (i < listSize) {
      T t = list.get(i);
      saved = saved && database.insert(name, null, get(t)) > 0;
      i++;
    }
    /*if (close) database.close();*/
    return saved;
  }

  @Override
  public final boolean onDrop(SQLiteDatabase database) throws ModelError {
    String sql = DROP_PREFIX + name + ";";
    try {
      database.execSQL(sql);
    } catch (SQLException e) {
      throw new ModelError(e);
    }
    return true;
  }

  @Override
  public final int onGetLastId(SQLiteDatabase database) {
    if (id == null) return 0;
    String[] cols = new String[]{id.getName()};
    Cursor cursor = database.query(name, cols, null, null, null, null, null);
    cursor.moveToLast();
    int id = cursor.getInt(Model.id.getIndex());
    cursor.close();
    /*database.close();*/
    return id;
  }

  @Override
  public void backup(SQLiteDatabase database) {
    backup = onReadAll(database, false);
  }

  @Override
  public void restore(SQLiteDatabase database) {
    if (backup != null && !backup.isEmpty()) onSave(backup, database, false);
  }

  public T getWithId(Cursor cursor) {
    T t = from(cursor);
    t.id(cursor.getInt(id.getIndex()));
    return t;
  }

  private abstract class QueryExtras<Q extends S>
      implements Extras<Q>, Converter<Q, Cursor, ContentValues> {

    private SQLiteDatabase database;

    QueryExtras(SQLiteDatabase database) {
      this.database = database;
    }

    public SQLiteDatabase database() {
      return database;
    }

    @Nullable
    @Override
    public Q first() {
      Cursor cursor;
      try {
        String sql = SELECT_PREFIX + name + " LIMIT `1`;";
        cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        Q t = from(cursor);
        cursor.close();
        /*database.close();*/
        return t;
      } catch (SQLiteException e) {
        LogUtil.e(TAG, e);
        return null;
      }
      /*return all().first();*/
    }

    @Nullable
    @Override
    public Q last() {
      Cursor cursor;
      try {
        String sql = SELECT_PREFIX + name + " ORDER BY 'id' DESC LIMIT `1`;";
        cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        Q t = from(cursor);
        cursor.close();
        /*database.close();*/
        return t;
      } catch (SQLiteException e) {
        LogUtil.e(TAG, e);
        return null;
      }
      /*return all().last();*/
    }

    @Override
    public SList<Q> all() {
      Cursor cursor;
      try {
        String sql = SELECT_PREFIX + name + ";";
        cursor = database.rawQuery(sql, null);
      } catch (SQLiteException e) {
        return new SList<>();
      }
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }

    @Override
    public SList<Q> limit(int limit) {
      QueryBuilder builder = new QueryBuilder().from(Model.this).take(limit);
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }

    @Override
    public SList<Q> between(Column<Integer> column, Integer a, Integer b) {
      QueryBuilder builder = new QueryBuilder().from(Model.this).whereAnd(Criteria.between(column, a, b));
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      return ts;
    }

    @Override
    public SList<Q> where(Column[] column) {
      QueryBuilder builder = new QueryBuilder().from(Model.this);
      for (Column column1 : column) builder.whereAnd(Criteria.equals(column1, column1.value()));
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> qs = new SList<>();
      while (cursor.moveToNext()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        qs.add(t);
      }
      cursor.close();
      return qs;
    }

    @Override
    public SList<Q> notIn(Column<Integer> column, Integer a, Integer b) {
      QueryBuilder builder = new QueryBuilder().from(Model.this).whereAnd(Criteria.notIn(column, new Object[]{a, b}));
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor;
      cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }

    @Override
    public SList<Q> like(Column<String>[] column) {
      QueryBuilder builder = new QueryBuilder().from(Model.this);
      for (Column column1 : column)
        builder.whereAnd(Criteria.contains(column1, (String) column1.value()));
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> qs = new SList<>();
      while (cursor.moveToNext()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        qs.add(t);
      }
      cursor.close();
      return qs;
    }

    @Override
    public SList<Q> orderBy(Column column) {
      QueryBuilder builder = new QueryBuilder().from(Model.this);
      builder = column.order().equals(Column.DESCENDING) ? builder.orderByDescending(column) : builder.orderByAscending(column);
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor;
      cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }

    @Override
    public SList<Q> groupBy(Column column) {
      QueryBuilder builder = new QueryBuilder().from(Model.this).groupBy(column);
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor;
      cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }

    @Override
    public SList<Q> groupAndOrderBy(Column column, Column column1) {
      QueryBuilder builder = new QueryBuilder().from(Model.this).groupBy(column);
      builder = column1.order().equals(Column.DESCENDING) ? builder.orderByDescending(column1) : builder.orderByAscending(column1);
      String query = builder.build();
      String[] parameters = builder.buildParameters();
      LogUtil.d(TAG, "query: "+query, "parameters: ", parameters);
      Cursor cursor;
      cursor = database.rawQuery(builder.build(), builder.buildParameters());
      SList<Q> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        Q t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database.close();*/
      return ts;
    }
  }

  private abstract class QueryExtras2<U extends S> extends QueryExtras<U> {
    private Column[] columns;

    QueryExtras2(SQLiteDatabase database, Column[] columns) {
      super(database);
      this.columns = columns;
    }

    @Nullable
    @Override
    public U first() {
      SList<U> list = all();
      if (!list.isEmpty()) return list.get(0);
      return null;
    }

    @Nullable
    @Override
    public U last() {
      SList<U> list = all();
      if (!list.isEmpty()) return list.get(list.size() - 1);
      return null;
    }

    @Override
    public SList<U> all() {
      if (columns == null || columns.length == 0) return super.all();
      String[] args = new String[columns.length];
      String selection = "";
      for (int i = 0; i < args.length; i++) {
        Column column = columns[i];
        String where = column.getName() + column.getOperand();
        selection = selection.concat(where);
        if (column.value() instanceof String) args[i] = "\"" + column.value() + "\"";
        else args[i] = column.value().toString();
        selection = selection.concat(args[i]);
        if (i < args.length - 1) selection = selection.concat(" AND ");
      }
      Cursor cursor = database().query(name, null, selection, null, null, null, null);
      SList<U> ts = new SList<>();
      while (cursor.moveToNext() && !cursor.isClosed()) {
        U t = from(cursor);
        t.id(cursor.getInt(id.getIndex()));
        ts.add(t);
      }
      cursor.close();
      /*database().close();*/
      return ts;
    }

    @Override
    public SList<U> limit(int limit) {
      return null;
    }

    @Override
    public SList<U> between(Column<Integer> column, Integer a, Integer b) {
      return super.between(column, a, b);
    }

    @Override
    public SList<U> where(Column[] column) {
      return null;
    }

    @Override
    public SList<U> notIn(Column<Integer> column, Integer a, Integer b) {
      return super.notIn(column, a, b);
    }

    @Override
    public SList<U> like(Column<String>[] column) {
      return null;
    }

    @Override
    public SList<U> orderBy(Column column) {
      return null;
    }

    @Override
    public SList<U> groupBy(Column column) {
      return null;
    }

    @Override
    public SList<U> groupAndOrderBy(Column column, Column column1) {
      return null;
    }
  }
}
