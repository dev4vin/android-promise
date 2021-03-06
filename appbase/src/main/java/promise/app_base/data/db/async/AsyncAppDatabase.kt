package promise.app_base.data.db.async

import android.database.sqlite.SQLiteDatabase
import io.reactivex.disposables.CompositeDisposable
import promise.Promise
import promise.app_base.data.db.TodoTable
import promise.app_base.error.AppError
import promise.app_base.models.Todo
import promise.app_base.scopes.AppScope
import promise.data.db.Corrupt
import promise.data.db.ReactiveFastDB
import promise.data.db.Table
import promise.data.db.query.QueryBuilder
import promise.model.List
import promise.model.Message
import promise.model.ResponseCallBack
import promise.model.SList
import javax.inject.Inject

@AppScope
class AsyncAppDatabase @Inject constructor() : ReactiveFastDB(DB_NAME, DB_VERSION, null,
    Corrupt { Promise.instance().send(Message(SENDER_TAG, "Database is corrupted")) }) {
  private val disposable = CompositeDisposable()

  override fun shouldUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean =
      newVersion > oldVersion

  override fun tables(): List<Table<*, SQLiteDatabase>> =
      object : List<Table<*, SQLiteDatabase>>() {
        init {
          add(todoTable)
        }
      }

  fun todos(skip: Int, limit: Int, responseCallBack: ResponseCallBack<List<Todo>, AppError>) {
    disposable.add(query(QueryBuilder().from(todoTable).skip(skip).take(limit))
        .subscribe({ cursor ->
          responseCallBack.response(object : List<Todo>() {
            init {
              while (cursor.moveToNext()) add(todoTable!!.from(cursor))
            }
          })
        }, { throwable ->
          responseCallBack.error(AppError(throwable))
        }))
  }

  /**
   * @param category
   */
  fun todos(category: String, responseCallBack: ResponseCallBack<List<Todo>, AppError>) {
    disposable.add(readAll(todoTable, TodoTable.category.with(category))
        .subscribe({ todos -> responseCallBack.response(todos) },
            { throwable -> responseCallBack.error(AppError(throwable)) }))
  }

  /**
   * @param todos to be saved
   */
  fun saveTodos(todos: List<Todo>, responseCallBack: ResponseCallBack<Boolean, AppError>) {
    disposable.add(save(SList(todos), todoTable).subscribe({ aBoolean -> responseCallBack.response(aBoolean) }, { throwable -> responseCallBack.error(AppError(throwable)) }))
  }

  override fun onTerminate(): CompositeDisposable? = disposable

  companion object {

    private const val DB_NAME = "a"
    private const val DB_VERSION = 1
    const val SENDER_TAG = "App_Database"
    private val todoTable: TodoTable by lazy { TodoTable() }

  }
}
