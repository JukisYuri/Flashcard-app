package com.example.mindcard.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        NotificationHelper.sendReminderNotification(
            context = context,
            title = "Time to study! 📚",
            message = "Open MindCard and review your flashcards."
        )

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily_reminder"

        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (target.before(now)) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }

            val delay = target.timeInMillis - now.timeInMillis

            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun reschedule(context: Context) {
            val (hour, minute) = NotificationHelper.getReminderTime(context)
            if (NotificationHelper.isReminderEnabled(context)) {
                schedule(context, hour, minute)
            }
        }
    }
}
