package com.fieldsyncpro

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.fieldsyncpro.domain.repository.AuthRepository
import com.fieldsyncpro.worker.TaskSyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FieldSyncProApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var authRepository: AuthRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // Only schedule background sync when a user is signed in.
        applicationScope.launch {
            authRepository.currentUser
                .map { user -> user != null }
                .distinctUntilChanged()
                .collect { isSignedIn ->
                    if (isSignedIn) {
                        TaskSyncWorker.schedule(workManager)
                    } else {
                        workManager.cancelUniqueWork(TaskSyncWorker.WORK_NAME)
                    }
                }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
