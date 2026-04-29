package br.dev.allan.controlefinanceiro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.allan.controlefinanceiro.data.dataStore.SettingsManager
import br.dev.allan.controlefinanceiro.domain.usecase.ObserveRemoteDataUseCase
import br.dev.allan.controlefinanceiro.domain.usecase.SyncLocalToRemoteUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val settingsManager: SettingsManager,
    private val syncLocalToRemoteUseCase: SyncLocalToRemoteUseCase,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    private val _searchOriginRoute = MutableStateFlow<Any?>(null)
    val searchOriginRoute = _searchOriginRoute.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSearchMode(enabled: Boolean, origin: Any? = null) {
        _isSearchMode.value = enabled
        if (enabled && origin != null) {
            _searchOriginRoute.value = origin
        } else if (!enabled) {
            _searchOriginRoute.value = null
        }
    }

    init {
        // Atualiza as infos sempre que o estado da auth mudar ou na inicialização
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _userName.value = user?.displayName ?: ""
            _userEmail.value = user?.email ?: ""
            _isUserLoggedIn.value = user != null
            // Não atualizamos a foto aqui para evitar loops com o refreshUserInfo
        }
        
        checkAuthIntegrity()
        refreshUserInfo()
    }

    fun refreshUserInfo() {
        viewModelScope.launch {
            try {
                auth.currentUser?.reload()?.await()
                val user = auth.currentUser
                _userName.value = user?.displayName ?: ""
                _userEmail.value = user?.email ?: ""
                _isUserLoggedIn.value = user != null
                
                val rawPhotoUrl = user?.photoUrl?.toString()
                if (rawPhotoUrl != null) {
                    // Adiciona um timestamp para forçar o Coil a recarregar a imagem ignorando o cache
                    val timestamp = System.currentTimeMillis()
                    val freshPhotoUrl = if (rawPhotoUrl.contains("?")) {
                        "$rawPhotoUrl&refresh=$timestamp"
                    } else {
                        "$rawPhotoUrl?refresh=$timestamp"
                    }
                    _userPhotoUrl.value = freshPhotoUrl
                } else {
                    _userPhotoUrl.value = null
                }
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }
    }

    private fun checkAuthIntegrity() {
        val user = auth.currentUser ?: return
        startSync()
        
        viewModelScope.launch {
            try {
                // Força o Firebase a verificar com o servidor se a conta ainda é válida
                user.reload().await()
            } catch (e: Exception) {
                // Se o reload falhar (ex: conta desativada ou excluída no console)
                auth.signOut()
                _logoutEvent.emit(Unit)
            }
        }
    }

    private fun startSync() {
        viewModelScope.launch {
            try {
                // Sincroniza o que está local -> remoto primeiro
                syncLocalToRemoteUseCase()
                
                // Começa a observar mudanças no Firestore para atualizar o local em tempo real
                observeRemoteDataUseCase()
            } catch (e: Exception) {
                // Falha silenciosa
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
