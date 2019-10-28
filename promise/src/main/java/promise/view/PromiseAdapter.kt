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

package promise.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.collection.ArrayMap
import androidx.recyclerview.widget.*
import promise.Promise
import promise.UIJobScheduler
import promise.data.log.LogUtil
import promise.model.Viewable
import promise.view.models.ViewableInstance
import kotlin.reflect.KClass
import promise.model.List
import promise.util.Conditions
import java.util.*

/**
 * PromiseAdapter is used for populating items in recycler view
 * You can create an instance of promise adapter by passing a predefined list of items, passing an onclick
 * listener to notify if an item is clicked

 */
open class PromiseAdapter<T>(list: List<T>, var listener: Listener<T>?) : RecyclerView.Adapter<PromiseAdapter<T>.Holder>() {

  private val TAG = LogUtil.makeTag(PromiseAdapter::class.java)
  /**
   * used to store items on lifecycle changes
   */
  private val AdapterItems = "__adapter_items__"
  /**
   * indexes all items for ease of updating and removing
   */
  private val indexer: Indexer
  /**
   * the list of items to view
   */
  private var list: List<ViewableInstance<T>>? = null
  /**
   * fires when an item is long pressed
   */
  var longClickListener: LongClickListener<T>? = null
  /**
   * fires when an item is swiped
   */
  private var swipeListener: Swipe<T>? = null

  private var alternatingColor = 0
  /**
   * fires when the item is ready to be bound the ui
   */
  private var onAfterInitListener: OnAfterInitListener? = null
  /**
   * holds viewable class for each item in the list
   */
  private var viewableClasses: MutableMap<String, KClass<out Viewable>>? = null

  var isReverse: Boolean
    get() = indexer.reverse
    /**
     * reverses items in the list
     */
    private set(reverse) = this.indexer.reverse(reverse)

  /**
   * secondary constructor for passing down the listener
   */
  constructor(listener: Listener<T>?) : this(List<T>(), listener)

  /**
   * passes viewable map class and listener
   */
  constructor(viewableClasses: Map<Class<*>, KClass<out Viewable>>, listener: Listener<T>?) : this(List<T>(), listener) {
    this.viewableClasses = ArrayMap()
    for ((key, value) in viewableClasses)
      this.viewableClasses!![key.simpleName] = value
  }

  /**
   * convert the list to viewable instances and initialize the index
   */
  init {
    this.list = Conditions.checkNotNull(list.map { ViewableInstance(it) })
    indexer = Indexer()
  }

  /* public void restoreViewState(Bundle instanceState) {
    List<Parcelable> items = new List<>(instanceState.getParcelableArrayList(AdapterItems));
    if (items.isEmpty()) return;
    this.list = items.map(parcelable -> (T) parcelable);
    indexList();
  }

  public void backupViewState(Bundle instanceState) {
    instanceState.putParcelableArrayList(AdapterItems, new ArrayList<>(list.map(viewHolder -> (Parcelable) viewHolder)));
  }*/

  @Deprecated("")
  fun destroyViewState() {

  }

  /**
   * pass a swipe listener
   *
   * @param swipeListener
   * @return
   */
  fun swipe(swipeListener: Swipe<T>): PromiseAdapter<T> {
    this.swipeListener = swipeListener
    return this
  }

  fun onAfterInitListener(onAfterInitListener: OnAfterInitListener): PromiseAdapter<*> {
    this.onAfterInitListener = onAfterInitListener
    return this
  }

  fun alternatingColor(color: Int): PromiseAdapter<T> {
    this.alternatingColor = color
    return this
  }

  open fun add(t: T) {
    indexer.add(Conditions.checkNotNull(t))
  }

  fun unshift(t: T) {
    indexer.unshift(Conditions.checkNotNull(t))
  }

  open fun add(list: List<T>) {
    indexer.add(Conditions.checkNotNull(list))
  }

  fun remove(t: T) {
    indexer.remove(Conditions.checkNotNull(t))
  }

  fun updateAll() {
    indexer.updateAll()
  }

  fun update(viewHolder: T) {
    indexer.update(Conditions.checkNotNull(viewHolder))
  }

  fun clear() {
    indexer.clear()
  }

  override fun getItemViewType(position: Int): Int {
    val viewableInstance = list!![position]
    if (viewableClasses != null) {
      val tClass = (viewableInstance.t as Any).javaClass
      if (viewableClasses!!.containsKey(tClass.simpleName)) {
        val kClass = viewableClasses!![tClass.simpleName]
        viewableInstance.viewClass = kClass
      }
    }
    val viewable = viewableInstance.viewable()
    val viewType = viewable.layout()
    Conditions.checkState(viewType != 0, "The layout resource for $viewable is not provided")
    return viewType
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
    val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
    if (onAfterInitListener != null) onAfterInitListener!!.onAfterInit(view)
    return Holder(view)
  }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)
    val manager = recyclerView.layoutManager
    if (manager is GridLayoutManager) {
      val manager1 = manager as GridLayoutManager?
      recyclerView.layoutManager = WrapContentGridLayoutManager(recyclerView.context, manager1!!.spanCount)
    } else if (manager is LinearLayoutManager)
      recyclerView.layoutManager = WrapContentLinearLayoutManager(recyclerView.context)
    /*if (recyclerView.getItemAnimator() != null) {
      recyclerView.setItemAnimator(new CustomItemAnimator());
    }*/
    if (swipeListener != null) {
      val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
          return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
          if (viewHolder is PromiseAdapter<*>.Holder) {
            val response: Response = object : Response {
              override fun call() {
                /*update(holder.viewHolder.getT());*/
              }
            }
            when (direction) {
              ItemTouchHelper.RIGHT -> swipeListener!!.onSwipeRight(viewHolder.viewableInstance.t as T, response)
              ItemTouchHelper.LEFT -> swipeListener!!.onSwipeLeft(viewHolder.viewableInstance.t as T, response)
            }
          }
        }
      }
      ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView)
    }
  }

  override fun onBindViewHolder(holder: Holder, position: Int) {
    val t = list!![position]
    UIJobScheduler.submitJob {
      if (alternatingColor != 0)
        if (position % 2 == 1) holder.view.setBackgroundColor(alternatingColor)
      holder.bind(t)
    }
  }

  override fun getItemCount(): Int = indexer.size()

  fun getList(): List<T> = list!!.map { it.t }

  fun setList(list: List<T>) {
    this.indexer.setList(list)
  }

  fun reverse() {
    indexer.reverse(true)
  }

  fun reverse(reverse: Boolean) {
    indexer.reverse(reverse)
  }

  interface Listener<T> {
    fun onClick(t: T, @IdRes id: Int)
  }

  interface OnAfterInitListener {
    fun onAfterInit(view: View)
  }

  interface Response {
    fun call()
  }

  interface Swipe<T> {
    fun onSwipeRight(t: T, response: Response)

    fun onSwipeLeft(t: T, response: Response)
  }

  interface LongClickListener<T> {
    fun onLongClick(t: T, @IdRes id: Int)
  }

  inner class Holder(var view: View) : RecyclerView.ViewHolder(view) {
    internal lateinit var viewableInstance: ViewableInstance<T>

    internal fun bind(viewableInstance: ViewableInstance<T>) {
      this.viewableInstance = viewableInstance
      this.viewableInstance.viewable().init(view)
      this.viewableInstance.viewable().bind(view)
      bindListener()
      bindLongClickListener()
    }

    private fun bindListener() {
      if (listener == null) return
      val kClass = viewableInstance.viewClass
      if (kClass != null) {
        val fields = listOf(*kClass.java.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewClassObject
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnClickListener { v -> listener!!.onClick(viewableInstance.t, v.id) }
          } catch (ignored: IllegalAccessException) {
            /*LogUtil.e(TAG, "illegal access ", ignored);*/
          }

      } else {
        val fields = listOf(*viewableInstance.viewable().javaClass.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewable()
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnClickListener { v -> listener!!.onClick(viewableInstance.t, v.id) }
          } catch (ignored: IllegalAccessException) {
            /*LogUtil.e(TAG, "illegal access ", ignored);*/
          }

      }
    }

    private fun bindLongClickListener() {
      if (longClickListener == null) return
      val kClass = viewableInstance.viewClass
      if (kClass != null) {
        val fields = listOf(*kClass.java.declaredFields)
        for (field in fields)
          try {
            val viewable = viewableInstance.viewClassObject
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnLongClickListener { v ->
                    longClickListener!!.onLongClick(viewableInstance.t, v.id)
                    true
                  }
          } catch (ignored: IllegalAccessException) {
          }

      } else {
        val fields = List(Arrays.asList(*viewableInstance.viewable().javaClass.declaredFields))
        for (field in fields)
          try {
            val viewable = viewableInstance.viewable()
            val view = field.get(viewable)
            if (view is View)
              view
                  .setOnLongClickListener { v ->
                    longClickListener!!.onLongClick(viewableInstance.t, v.id)
                    true
                  }
          } catch (ignored: IllegalAccessException) {
          }

      }
    }
  }

  private inner class Indexer {
    internal var reverse = false

    internal fun add(t: T) {
      if (list == null) list = List()
      if (!list!!.isEmpty()) {
        if (reverse) list!!.reverse()
        val instance = ViewableInstance(t)
        list!!.add(instance)
        if (reverse) list!!.reverse()
        Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
      } else {
        val instance = ViewableInstance(t)
        list!!.add(instance)
        Promise.instance().executeOnUi { notifyItemInserted(0) }
      }
    }

    internal fun unshift(t: T) {
      if (list == null) list = List()
      if (!list!!.isEmpty()) {
        val list1 = List<T>()
        list1.add(t)
        list1.addAll(list!!.map { it.t })
        setList(list1)
        Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
      } else {
        add(t)
      }
    }

    internal fun setList(list: List<T>) {
      this@PromiseAdapter.list = list.map { ViewableInstance(it) }
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal fun remove(t: T) {
      if (list == null) return
      val instance = list!!.find { i -> i.t === t }
      list!!.remove(instance)
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal fun update(viewHolder: T) {
      /* if (list == null) return;
      ViewableInstance<T> v = list.find(i -> i.getT() == viewHolder);
      if (v == null) return;
      if (v.viewable().index() >= list.size()) return;
      list.set(v.viewable().index(), v);
      notifyItemChanged(viewHolder.index());
      Promise.instance().executeOnUi(PromiseAdapter.this::notifyDataSetChanged);*/
    }

    internal fun updateAll() {
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal fun add(list: List<T>) {
      for (t in list) add(t)
    }

    internal fun clear() {
      if (list == null || list!!.isEmpty()) return
      list!!.clear()
      Promise.instance().executeOnUi { this@PromiseAdapter.notifyDataSetChanged() }
    }

    internal fun size(): Int = if (list == null || list!!.isEmpty()) 0 else list!!.size

    internal fun reverse(reverse: Boolean) {
      this.reverse = reverse
    }
  }

  inner class WrapContentLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
      try {
        super.onLayoutChildren(recycler, state)
      } catch (e: IndexOutOfBoundsException) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView")
      }

    }
  }

  inner class WrapContentGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
      try {
        super.onLayoutChildren(recycler, state)
      } catch (e: IndexOutOfBoundsException) {
        LogUtil.e(TAG, "meet a Bug in RecyclerView")
      }

    }
  }

  inner class CustomItemAnimator : DefaultItemAnimator() {
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int): Boolean {
      if (supportsChangeAnimations) {
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
      } else {
        if (oldHolder === newHolder) {
          if (oldHolder != null) {
            // if the two holders are equal, call dispatch change only once
            dispatchChangeFinished(oldHolder, /*ignored*/ true)
          }
        } else {
          // else call dispatch change once for every non-null holder
          if (oldHolder != null) {
            dispatchChangeFinished(oldHolder, true)
          }
          if (newHolder != null) {
            dispatchChangeFinished(newHolder, false)
          }
        }
        // we don'viewHolder need a call to requestPendingTransactions after this, return false.
        return false
      }
    }
  }
}
