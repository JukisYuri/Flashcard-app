package com.example.mindcard.data

import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.pow

/**
 * FSRS (Free Spaced Repetition Scheduler) Algorithm
 * Reference: https://github.com/open-spaced-repetition/fsrs4anki
 *
 * Rating:
 *   1 = Again (forgot)
 *   2 = Hard
 *   3 = Good
 *   4 = Easy
 */

object FsrsDefaults {
    const val DEFAULT_EASE_FACTOR = 2.5
    const val DEFAULT_INTERVAL = 0.0 // days
    const val DEFAULT_REPETITIONS = 0
    const val GRADUATING_INTERVAL = 1.0
    const val EASY_INTERVAL = 4.0
    val LEARNING_STEPS = doubleArrayOf(1.0, 10.0) // minutes
    val RELEARNING_STEPS = doubleArrayOf(10.0)
}

data class CardState(
    val easeFactor: Double = FsrsDefaults.DEFAULT_EASE_FACTOR,
    val interval: Double = FsrsDefaults.DEFAULT_INTERVAL,
    val repetitions: Int = FsrsDefaults.DEFAULT_REPETITIONS,
    val nextReview: Long = System.currentTimeMillis(),
    val lastReview: Long = 0L,
    val state: ReviewState = ReviewState.New
)

enum class ReviewState {
    New,        // Chưa học
    Learning,   // Đang học (mới)
    Review,     // Đã học, đang ôn
    Relearning  // Quên, cần học lại
}

enum class Rating(val value: Int) {
    Again(1),
    Hard(2),
    Good(3),
    Easy(4)
}

object FsrsAlgorithm {

    /**
     * Tính toán trạng thái mới của card sau khi user đánh giá
     */
    fun nextCardState(
        card: CardState,
        rating: Rating,
        now: Long = System.currentTimeMillis()
    ): CardState {
        return when (card.state) {
            ReviewState.New -> handleNew(card, rating, now)
            ReviewState.Learning -> handleLearning(card, rating, now)
            ReviewState.Review -> handleReview(card, rating, now)
            ReviewState.Relearning -> handleRelearning(card, rating, now)
        }
    }

    private fun handleNew(card: CardState, rating: Rating, now: Long): CardState {
        return when (rating) {
            Rating.Good -> {
                if (FsrsDefaults.LEARNING_STEPS.size > 1) {
                    card.copy(
                        state = ReviewState.Learning,
                        nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[1]),
                        lastReview = now
                    )
                } else {
                    card.copy(
                        state = ReviewState.Review,
                        interval = FsrsDefaults.GRADUATING_INTERVAL,
                        repetitions = 1,
                        nextReview = now + daysToMillis(FsrsDefaults.GRADUATING_INTERVAL),
                        lastReview = now
                    )
                }
            }
            Rating.Easy -> {
                card.copy(
                    state = ReviewState.Review,
                    interval = FsrsDefaults.EASY_INTERVAL,
                    easeFactor = max(1.3, card.easeFactor + 0.15),
                    repetitions = 2,
                    nextReview = now + daysToMillis(FsrsDefaults.EASY_INTERVAL),
                    lastReview = now
                )
            }
            Rating.Hard -> {
                card.copy(
                    state = ReviewState.Learning,
                    nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Again -> {
                card.copy(
                    state = ReviewState.Learning,
                    nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[0]),
                    lastReview = now
                )
            }
        }
    }

    private fun handleLearning(card: CardState, rating: Rating, now: Long): CardState {
        val currentStep = getCurrentLearningStep(card, FsrsDefaults.LEARNING_STEPS)

        return when (rating) {
            Rating.Again -> {
                card.copy(
                    state = ReviewState.Learning,
                    nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Hard -> {
                card.copy(
                    state = ReviewState.Learning,
                    nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Good -> {
                val nextStep = currentStep + 1
                if (nextStep < FsrsDefaults.LEARNING_STEPS.size) {
                    card.copy(
                        state = ReviewState.Learning,
                        nextReview = now + minutesToMillis(FsrsDefaults.LEARNING_STEPS[nextStep]),
                        lastReview = now
                    )
                } else {
                    // Graduate to review
                    card.copy(
                        state = ReviewState.Review,
                        interval = FsrsDefaults.GRADUATING_INTERVAL,
                        repetitions = 1,
                        nextReview = now + daysToMillis(FsrsDefaults.GRADUATING_INTERVAL),
                        lastReview = now
                    )
                }
            }
            Rating.Easy -> {
                card.copy(
                    state = ReviewState.Review,
                    interval = FsrsDefaults.EASY_INTERVAL,
                    easeFactor = max(1.3, card.easeFactor + 0.15),
                    repetitions = 2,
                    nextReview = now + daysToMillis(FsrsDefaults.EASY_INTERVAL),
                    lastReview = now
                )
            }
        }
    }

    private fun handleReview(card: CardState, rating: Rating, now: Long): CardState {
        val stability = card.interval
        val newEaseFactor = when (rating) {
            Rating.Again -> max(1.3, card.easeFactor - 0.2)
            Rating.Hard -> max(1.3, card.easeFactor - 0.15)
            Rating.Good -> card.easeFactor
            Rating.Easy -> card.easeFactor + 0.15
        }

        val newInterval = when (rating) {
            Rating.Again -> {
                card.copy(
                    state = ReviewState.Relearning,
                    nextReview = now + minutesToMillis(FsrsDefaults.RELEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Hard -> {
                val newInt = max(1.0, stability * 1.2)
                card.copy(
                    easeFactor = newEaseFactor,
                    interval = newInt,
                    repetitions = card.repetitions + 1,
                    nextReview = now + daysToMillis(newInt),
                    lastReview = now,
                    state = ReviewState.Review
                )
            }
            Rating.Good -> {
                val newInt = if (card.repetitions == 0) {
                    FsrsDefaults.GRADUATING_INTERVAL
                } else if (card.repetitions == 1) {
                    6.0
                } else {
                    max(1.0, stability * newEaseFactor)
                }
                card.copy(
                    easeFactor = newEaseFactor,
                    interval = newInt,
                    repetitions = card.repetitions + 1,
                    nextReview = now + daysToMillis(newInt),
                    lastReview = now,
                    state = ReviewState.Review
                )
            }
            Rating.Easy -> {
                val newInt = max(1.0, stability * newEaseFactor * 1.3)
                card.copy(
                    easeFactor = newEaseFactor,
                    interval = newInt,
                    repetitions = card.repetitions + 1,
                    nextReview = now + daysToMillis(newInt),
                    lastReview = now,
                    state = ReviewState.Review
                )
            }
        }

        return newInterval
    }

    private fun handleRelearning(card: CardState, rating: Rating, now: Long): CardState {
        val currentStep = getCurrentLearningStep(card, FsrsDefaults.RELEARNING_STEPS)

        return when (rating) {
            Rating.Again -> {
                card.copy(
                    state = ReviewState.Relearning,
                    nextReview = now + minutesToMillis(FsrsDefaults.RELEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Hard -> {
                card.copy(
                    state = ReviewState.Relearning,
                    nextReview = now + minutesToMillis(FsrsDefaults.RELEARNING_STEPS[0]),
                    lastReview = now
                )
            }
            Rating.Good -> {
                val nextStep = currentStep + 1
                if (nextStep < FsrsDefaults.RELEARNING_STEPS.size) {
                    card.copy(
                        state = ReviewState.Relearning,
                        nextReview = now + minutesToMillis(FsrsDefaults.RELEARNING_STEPS[nextStep]),
                        lastReview = now
                    )
                } else {
                    // Return to review
                    val newInt = max(1.0, card.interval * card.easeFactor)
                    card.copy(
                        state = ReviewState.Review,
                        interval = newInt,
                        nextReview = now + daysToMillis(newInt),
                        lastReview = now
                    )
                }
            }
            Rating.Easy -> {
                val newInt = max(1.0, card.interval * card.easeFactor * 1.3)
                card.copy(
                    state = ReviewState.Review,
                    interval = newInt,
                    easeFactor = max(1.3, card.easeFactor + 0.15),
                    nextReview = now + daysToMillis(newInt),
                    lastReview = now
                )
            }
        }
    }

    private fun getCurrentLearningStep(card: CardState, steps: DoubleArray): Int {
        val lastReview = card.lastReview
        if (lastReview == 0L) return 0

        val elapsed = System.currentTimeMillis() - lastReview
        var step = 0
        for (i in steps.indices) {
            if (elapsed >= minutesToMillis(steps[i])) {
                step = i
            }
        }
        return step
    }

    fun daysToMillis(days: Double): Long {
        return TimeUnit.DAYS.toMillis(days.toLong())
    }

    fun minutesToMillis(minutes: Double): Long {
        return TimeUnit.MINUTES.toMillis(minutes.toLong())
    }

    /**
     * Kiểm tra card có cần ôn lại không
     */
    fun isDue(card: CardState, now: Long = System.currentTimeMillis()): Boolean {
        return card.nextReview <= now
    }

    /**
     * Tính phần trăm mastered (đã thuộc) dựa trên interval
     * Card có interval >= 21 ngày được coi là "mastered"
     */
    fun calculateMastery(cards: List<Card>): Int {
        if (cards.isEmpty()) return 0
        val masteredCount = cards.count { card ->
            card.interval >= 21.0 || card.repetitions >= 3
        }
        return ((masteredCount.toDouble() / cards.size) * 100).toInt()
    }

    /**
     * Lấy số lượng card đến hạn ôn
     */
    fun getDueCardsCount(cards: List<Card>): Int {
        return cards.count { card ->
            val state = CardState(
                easeFactor = card.easeFactor,
                interval = card.interval,
                repetitions = card.repetitions,
                nextReview = card.nextReview,
                lastReview = card.lastReview,
                state = ReviewState.entries.find { it.name == card.reviewState } ?: ReviewState.New
            )
            isDue(state)
        }
    }
}
