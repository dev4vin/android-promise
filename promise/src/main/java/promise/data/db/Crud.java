/*
 *
 *  * Copyright 2017, Peter Vincent
 *  * Licensed under the Apache License, Version 2.0, Promise.
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package promise.data.db;


import promise.model.Identifiable;
import promise.model.List;
import promise.model.SList;


interface Crud<X> {

    <T extends Identifiable<Integer>> Table.Extras<T> read(Table<T, X> table);

    <T extends Identifiable<Integer>> SList<T> readAll(Table<T, X> table);

    <T extends Identifiable<Integer>> SList<T> readAll(Table<T, X> table, Column column);

    <T extends Identifiable<Integer>> boolean update(T t, Table<T, X> table, Column column);

    <T extends Identifiable<Integer>> boolean update(T t, Table<T, X> table);

    <T extends Identifiable<Integer>> SList<T> readAll(Table<T, X> table, Column[] columns);

    <T extends Identifiable<Integer>> Table.Extras<T> read(Table<T, X> table, Column... columns);

    boolean delete(Table<?, X> table, Column column);

    <T extends Identifiable<Integer>> boolean delete(Table<T, X> table, T t);

    boolean delete(Table<?, X> table);

    <T> boolean delete(Table<?, X> table, Column<T> column, List<T> list);

    <T extends Identifiable<Integer>> long save(T t, Table<T, X> table);

    <T extends Identifiable<Integer>> boolean save(SList<T> list, Table<T, X> table);

    boolean deleteAll();

    int getLastId(Table<?, X> table);
}
