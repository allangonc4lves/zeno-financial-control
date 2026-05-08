package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import br.dev.allan.controlefinanceiro.domain.usecase.SyncDataUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.SyncLocalToRemoteUseCase
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val syncDataUseCase: SyncDataUseCase,
    private val syncLocalToRemoteUseCase: SyncLocalToRemoteUseCase
) : ViewModel() {

    fun signInWithGoogle(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("938011484871-s1k46cus5dmg115f26ueuth8l812dr0u.apps.googleusercontent.com")
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                android.util.Log.d("LoginDebug", "Credencial obtida! Tipo: ${credential.type}")

                if (credential is GoogleIdTokenCredential) {
                    android.util.Log.d("LoginDebug", "É GoogleIdTokenCredential. Iniciando Firebase Auth...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
                    val authResult = auth.signInWithCredential(firebaseCredential).await()
                    android.util.Log.d("LoginDebug", "Firebase Auth sucesso: ${authResult.user?.email}")

                    // Cria o documento do usuário no Firestore se ele não existir
                    val user = authResult.user
                    if (user != null) {
                        val userDoc = mapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "displayName" to user.displayName,
                            "photoUrl" to user.photoUrl?.toString(),
                            "lastLogin" to System.currentTimeMillis()
                        )
                        firestore.collection("users").document(user.uid).set(userDoc).await()
                    }

                    android.util.Log.d("LoginDebug", "Iniciando Sincronização...")
                    syncLocalToRemoteUseCase()
                    syncDataUseCase()
                    android.util.Log.d("LoginDebug", "Sincronização concluída. Chamando onSuccess.")
                    onSuccess()
                } else {
                    android.util.Log.w("LoginDebug", "Credencial obtida NÃO é GoogleIdTokenCredential. Tipo real: ${credential.type}")
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        android.util.Log.d("LoginDebug", "Conversão manual para GoogleIdTokenCredential funcionou!")
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        auth.signInWithCredential(firebaseCredential).await()
                        syncLocalToRemoteUseCase()
                        syncDataUseCase()
                        onSuccess()
                    } catch (e: Exception) {
                        android.util.Log.e("LoginDebug", "Falha na conversão manual: ${e.message}")
                    }
                }
            } catch (e: GetCredentialException) {
                android.util.Log.e("LoginError", "Erro CredentialManager (Tipo: ${e.type}): ${e.message}")
                if (e.message?.contains("No credentials available") == true) {
                    android.util.Log.e("LoginError", "DICA: Verifique se o SHA-1 está no Firebase e se o Web Client ID está correto.")
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginError", "Erro genérico no login: ${e.message}", e)
            }
        }
    }
    
    fun logout(context: Context, onLogoutSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                auth.signOut()
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                onLogoutSuccess()
            } catch (e: Exception) {
                // Log error if needed
            }
        }
    }
}
