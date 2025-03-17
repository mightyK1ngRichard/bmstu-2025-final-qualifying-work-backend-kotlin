package com.git.mightyK1ngRichard.transport.feedback

import com.git.mightyK1ngRichard.transport.feedback.models.AddFeedback
import com.git.mightyK1ngRichard.transport.feedback.models.Feedback
import io.ktor.server.application.*

interface FeedbackController {
    suspend fun getProductFeedbacks(call: ApplicationCall)
    suspend fun addFeedback(call: ApplicationCall)
}

interface FeedbackUseCase {
    suspend fun getProductFeedbacks(productID: String): List<Feedback>
    suspend fun addFeedback(req: AddFeedback.FeedbackContent): AddFeedback.Response
}

interface FeedbackRepository {
    suspend fun getProductFeedbacks(productID: String): List<Feedback>
    suspend fun addFeedback(req: AddFeedback.FeedbackContent): AddFeedback.Response
}
