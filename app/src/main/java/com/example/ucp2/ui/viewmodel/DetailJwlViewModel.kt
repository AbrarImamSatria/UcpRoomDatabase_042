package com.example.ucp2.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ucp2.data.entity.Jadwal
import com.example.ucp2.repository.RepositoryJwl
import com.example.ucp2.ui.navigation.DestinasiDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailjwlViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositoryJwl: RepositoryJwl,
) : ViewModel() {
    private val _id: Long = checkNotNull(savedStateHandle.get<Long>(DestinasiDetail.ID))

    val detailUiState: StateFlow<DetailUiState> = repositoryJwl.getJwl(_id)
        .filterNotNull()
        .map {
            DetailUiState(
                detailUiEvent = it.toDetailUiEvent(),
                isLoading = false,
            )
        }
        .onStart {
            emit(DetailUiState(isLoading = true))
            delay(600)
        }
        .catch {
            emit(
                DetailUiState(
                    isLoading = false,
                    isError = true,
                    errorMessage = it.message ?: "Terjadi Kesalahan",
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2000),
            initialValue = DetailUiState(
                isLoading = true,
            ),
        )

    fun deleteJwl() {
        detailUiState.value.detailUiEvent.toJadwalEntity().let {
            viewModelScope.launch {
                repositoryJwl.deleteJwl(it)
            }
        }
    }
}
data class DetailUiState (
    val detailUiEvent: JadwalEvent = JadwalEvent(),
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = ""
) {
    val isUiEventEmpty: Boolean
        get() = detailUiEvent == JadwalEvent()

    val isUiEventNotEmpty: Boolean
        get() = detailUiEvent != JadwalEvent()
}

/*
* Data class untuk menampung data yang akan ditampilkan di UI*/

//memindahkan data dari entity ke ui
fun Jadwal.toDetailUiEvent() : JadwalEvent {
    return JadwalEvent(
        id = id,
        namaDokter = namaDokter,
        namaPasien = namaPasien,
        noHp = noHp,
        tanggalKonsultasi = tanggalKonsultasi,
        status = status
    )
}