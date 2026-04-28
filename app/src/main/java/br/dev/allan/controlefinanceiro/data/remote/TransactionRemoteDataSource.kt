package br.dev.allan.controlefinanceiro.data.remote

import br.dev.allan.controlefinanceiro.data.remote.mapper.toDto
import br.dev.allan.controlefinanceiro.data.remote.model.TransactionDto
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TransactionRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val collectionPath = "transactions"

    fun observeTransactions(): Flow<List<TransactionDto>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.emptyFlow()
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(TransactionDto::class.java)
            }
    }

    suspend fun saveTransaction(transaction: Transaction) {
        val userId = auth.currentUser?.uid ?: return
        val dto = transaction.toDto(userId)
        
        android.util.Log.d("FirestoreSync", "Tentando salvar transação: ${dto.id} para o usuário: $userId")
        
        try {
            firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(dto.id)
                .set(dto)
                .await()
            android.util.Log.d("FirestoreSync", "Transação salva com sucesso no Firestore: ${dto.id}")
        } catch (e: Exception) {
            android.util.Log.e("FirestoreSync", "Erro ao salvar transação no Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteTransaction(transactionId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .document(transactionId)
            .delete()
            .await()
    }

    suspend fun deleteTransactions(transactionIds: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()

        transactionIds.forEach { id ->
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(id)
            batch.delete(docRef)
        }

        batch.commit().await()
    }

    suspend fun syncTransactions(transactions: List<Transaction>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        
        transactions.forEach { transaction ->
            val dto = transaction.toDto(userId)
            val docRef = firestore.collection("users")
                .document(userId)
                .collection(collectionPath)
                .document(dto.id)
            batch.set(docRef, dto)
        }
        
        batch.commit().await()
    }

    suspend fun fetchAllTransactions(): List<TransactionDto> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("users")
            .document(userId)
            .collection(collectionPath)
            .get()
            .await()
            .toObjects(TransactionDto::class.java)
    }
}
