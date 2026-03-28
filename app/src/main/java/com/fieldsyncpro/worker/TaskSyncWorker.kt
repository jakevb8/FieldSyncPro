package com.fieldsyncpro.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.fieldsyncpro.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic background worker that pushes local-only tasks to the server
 * and pulls any remote changes into Room.
 *
 * Scheduled via [TaskSyncWorker.schedule] — runs every 15 minutes when
 * network is available.
 */
@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val success = taskRepository.syncTasks()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME   = "task_sync_worker"
        private const val MAX_RETRIES = 3

        /**
         * Enqueue a periodic [TaskSyncWorker] that runs every 15 minutes
         * and requires network connectivity.
         */
        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<TaskSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Run a one-shot sync immediately (e.g., triggered by the user tapping Refresh).
         */
        fun runNow(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<TaskSyncWorker>()
                .setConstraints(constraints)
                .build()

            workManager.enqueue(request)
        }
    }
}
