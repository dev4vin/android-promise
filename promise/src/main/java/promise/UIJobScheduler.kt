package promise


import android.os.Build
import android.view.Choreographer
import java.util.*

object UIJobScheduler {
  private const val MAX_JOB_TIME_MS: Float = 4f

  private var elapsed = 0L
  private val jobQueue = ArrayDeque<() -> Unit>()
  private val isOverMaxTime get() = elapsed > MAX_JOB_TIME_MS * 1_000_000

  fun submitJob(job: () -> Unit) {
    jobQueue.add(job)
    if (jobQueue.size == 1) {
      Promise.instance().executeOnUi { processJobs() }
    }
  }

  private fun processJobs() {
    while (!jobQueue.isEmpty() && !isOverMaxTime) {
      val start = System.nanoTime()
      jobQueue.poll().invoke()
      elapsed += System.nanoTime() - start
    }
    if (jobQueue.isEmpty()) {
      elapsed = 0
    } else if (isOverMaxTime) {
      onNextFrame {
        elapsed = 0
        processJobs()
      }
    }
  }

  private fun onNextFrame(callback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      Choreographer.getInstance().postFrameCallback { callback() }
    } else Promise.instance().executeOnUi { callback() }
  }
}