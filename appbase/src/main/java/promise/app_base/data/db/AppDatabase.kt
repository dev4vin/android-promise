package promise.app_base.data.db

import android.database.sqlite.SQLiteDatabase
import promise.Promise
import promise.app_base.models.Task
import promise.app_base.models.Todo
import promise.app_base.scopes.AppScope
import promise.data.db.Corrupt
import promise.data.db.FastDB
import promise.data.db.Table
import promise.data.db.query.QueryBuilder
import promise.data.log.LogUtil
import promise.model.List
import promise.model.Message
import promise.model.SList
import javax.inject.Inject

@AppScope
class AppDatabase @Inject constructor() : FastDB(DB_NAME, DB_VERSION, null,
    Corrupt { Promise.instance().send(Message(SENDER_TAG, "Database is corrupted")) }) {
  private val TAG = LogUtil.makeTag(AppDatabase::class.java)

  /**
   * check if the database should be upgraded
   *
   * @param database   to be upgraded
   * @param oldVersion current database version
   * @param newVersion new database version
   * @return true if to be upgraded or false
   */
  override fun shouldUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
    if (oldVersion == 1 && newVersion == 2) add(database, List.fromArray(taskTable))
    return newVersion > oldVersion
  }

  /**
   * All tables must be registered here
   *
   * @return the list of tables to be created by fast db
   */
  override fun tables(): List<Table<*, SQLiteDatabase>> =
      object : List<Table<*, SQLiteDatabase>>() {
        init {
          addAll(fromArray(
              todoTable,
              taskTable
          ))
        }
      }

  /**
   * @param skip  records to skip
   * @param limit records to take
   * @return a list of todos
   */
  fun todos(skip: Int, limit: Int): List<Todo> = object : List<Todo>() {
    init {
      val cursor = query(QueryBuilder().from(todoTable).skip(skip).take(limit))
      while (cursor.moveToNext()) add(todoTable!!.from(cursor))
    }
  }

  /**
   * @param category todo category
   * @return a list of todos matching this category
   */
  fun todos(category: String): List<Todo> = readAll(todoTable, TodoTable.category.with(category))

  fun tasks(category: String): List<Task> = readAll(taskTable, TaskTable.category.with(category))

  /**
   * @param todos to be saved
   * @return true if all todos are saved
   */
  fun saveTodos(todos: List<Todo>): Boolean = save(SList(todos), todoTable)

  fun addTodo(todo: Todo): Long = save(todo, todoTable)

  fun updateTodo(todo: Todo): Boolean = update(todo, todoTable)

  fun deleteTodo(todo: Todo): Boolean = delete(todoTable, todo)

  fun clearTodos(): Boolean = delete(todoTable)

  companion object {

    private const val DB_NAME = "a"
    private const val DB_VERSION = 2
    const val SENDER_TAG = "App_Database"

    private val todoTable: TodoTable by lazy { TodoTable() }
    private val taskTable: TaskTable by lazy { TaskTable() }

  }
}
