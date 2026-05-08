package br.dev.allan.controlefinanceiro.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.domain.usecase.ObserveRemoteDataUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.SyncDataUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.SyncLocalToRemoteUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val syncLocalToRemoteUseCase: SyncLocalToRemoteUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val observeRemoteDataUseCase: ObserveRemoteDataUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userName = MutableStateFlow(auth.currentUser?.displayName ?: "")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(auth.currentUser?.email ?: "")
    val userEmail = _userEmail.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow(auth.currentUser?.photoUrl?.toString())
    val userPhotoUrl = _userPhotoUrl.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        // Observa a URL persistida no DataStore para garantir UX offline consistente
        viewModelScope.launch {
            settingsManager.userPhotoUrl.collect { savedUrl ->
                if (savedUrl != null) {
                    _userPhotoUrl.value = savedUrl
                }
            }
        }

        // Inicializa o estado com o valor atual, mas garante que o listener trate a primeira carga
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            
            // Atualiza dados básicos imediatamente
            _userName.value = user?.displayName ?: ""
            _userEmail.value = user?.email ?: ""
            _isUserLoggedIn.value = user != null
            
            if (user != null) {
                // Se não temos nada no DataStore ainda, usa o que o Firebase der (pode ser a URL antiga)
                if (_userPhotoUrl.value == null) {
                    _userPhotoUrl.value = user.photoUrl?.toString()
                }
                refreshUserInfo()
                startSync()
            }
        }
        
        // Verificação de integridade apenas se já começar logado
        if (auth.currentUser != null) {
            checkAuthIntegrity()
        }
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            
            try {
                // Tenta atualizar do servidor
                user.reload().await()
                val updatedUser = auth.currentUser
                _userName.value = updatedUser?.displayName ?: ""
                _userEmail.value = updatedUser?.email ?: ""
                
                val rawPhotoUrl = updatedUser?.photoUrl?.toString()
                if (rawPhotoUrl != null) {
                    if (isOnline()) {
                        // Online: gera nova URL com timestamp e persiste
                        val timestamp = System.currentTimeMillis()
                        val freshPhotoUrl = if (rawPhotoUrl.contains("?")) {
                            "$rawPhotoUrl&refresh=$timestamp"
                        } else {
                            "$rawPhotoUrl?refresh=$timestamp"
                        }
                        _userPhotoUrl.value = freshPhotoUrl
                        settingsManager.setUserPhotoUrl(freshPhotoUrl)
                    } else {
                        // Offline: Mantém o que já está no State (que veio do DataStore no init)
                        // ou usa a rawPhotoUrl se o State estiver nulo
                        if (_userPhotoUrl.value == null) {
                            _userPhotoUrl.value = rawPhotoUrl
                        }
                    }
                }
            } catch (e: Exception) {
                // Se falhar (ex: offline), o estado já foi inicializado com o DataStore no init
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkAuthIntegrity() {
        val user = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                // Força o Firebase a verificar com o servidor se a conta ainda é válida
                user.reload().await()
            } catch (e: Exception) {
                // Lógica robusta para detectar falta de internet
                val isNetworkError = e is FirebaseNetworkException || 
                                    e.message?.contains("network", ignoreCase = true) == true ||
                                    e.cause is java.net.UnknownHostException ||
                                    e.cause is java.net.ConnectException
                
                if (!isNetworkError) {
                    // Erro crítico de conta (ex: token expirado ou revogado), desloga
                    auth.signOut()
                    _logoutEvent.emit(Unit)
                }
                // Se for erro de rede, não faz nada e deixa o usuário continuar offline
            }
        }
    }

    private fun startSync() {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            Log.e("SyncDebug", "Abortando sync: UID nulo")
            return
        }

        viewModelScope.launch {
            try {
                // Pequeno delay para garantir que o token de autenticação esteja pronto no SDK do Firestore
                kotlinx.coroutines.delay(1000)
                
                Log.d("SyncDebug", "Iniciando sync para UID: $currentUid")
                // Tenta baixar os dados remotos primeiro (Sincronização Inicial/Full)
                syncDataUseCase()

                // Sincroniza o que está local -> remoto (caso tenha ficado algo pendente no worker ou manual)
                syncLocalToRemoteUseCase()
                
                // Começa a observar mudanças no Firestore para atualizar o local em tempo real
                observeRemoteDataUseCase()
            } catch (e: Exception) {
                Log.e("SyncDebug", "Erro ao iniciar sincronização: ${e.message}")
            }
        }
    }

    val currencyCode = settingsManager.currencyCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "BRL")

    fun updateCurrency(code: String) {
        viewModelScope.launch {
            settingsManager.setCurrencyCode(code)
        }
    }
}
