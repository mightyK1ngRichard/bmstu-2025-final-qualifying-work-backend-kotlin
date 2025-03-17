package com.git.mightyK1ngRichard.transport.feedback

import com.git.mightyK1ngRichard.transport.feedback.models.AddFeedback
import com.git.mightyK1ngRichard.transport.feedback.models.Feedback
import com.git.mightyK1ngRichard.models.DatabaseException
import com.git.mightyK1ngRichard.pkg.managers.DatabaseFactory
import java.sql.SQLException
import java.sql.Connection
import java.util.*

class FeedbackRepositoryImpl(private val connection: Connection) : FeedbackRepository {
    companion object {
        private const val SELECT_ALL_FEEDBACKS = """
            SELECT f.id, f.text, f.date_creation, f.rating, 
                   u.id as user_id, u.fio, u.image_url
            FROM "feedback" f
            LEFT JOIN "user" u ON f.author_id = u.id
            WHERE cake_id = ?::uuid;
        """
        private const val ADD_FEEDBACK = """
            INSERT INTO "feedback" (id, text, rating, cake_id, author_id)
            VALUES (?::uuid, ?, ?, ?::uuid, ?::uuid);
        """
    }

    override suspend fun getProductFeedbacks(productID: String): List<Feedback> {
        val feedbacks = mutableListOf<Feedback>()
        try {
            connection.prepareStatement(SELECT_ALL_FEEDBACKS).use { statement ->
                statement.setString(1, productID)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    feedbacks.add(
                        Feedback(
                            uid = resultSet.getString("id"),
                            text = resultSet.getString("text"),
                            dateCreation = resultSet.getTimestamp("date_creation").toInstant(),
                            rating = resultSet.getInt("rating"),
                            author = Feedback.Author(
                                id = resultSet.getString("user_id"),
                                fio = resultSet.getString("fio"),
                                imageURL = resultSet.getString("image_url"),
                            ),
                        )
                    )
                }
            }
            return feedbacks
        } catch (e: SQLException) {
            throw DatabaseException("Error executing query: ${e.message}", e)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addFeedback(req: AddFeedback.FeedbackContent): AddFeedback.Response {
        val connection = DatabaseFactory.getConnection()
        try {
            val feedbackUid = UUID.randomUUID().toString()
            connection.prepareStatement(ADD_FEEDBACK).use { statement ->
                statement.setString(1, feedbackUid)
                statement.setString(2, req.text)
                statement.setInt(3, req.rating)
                statement.setString(4, req.cakeUid)
                statement.setString(5, req.authorUid)
                statement.executeUpdate()
            }
            return AddFeedback.Response(feedbackUid)
        } catch (e: SQLException) {
            throw DatabaseException("Error executing query: ${e.message}", e)
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close()
        }
    }
}
