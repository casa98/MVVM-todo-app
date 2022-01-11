package com.casa98.anotherlocalnotes.ui.add_edit_todo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casa98.anotherlocalnotes.data.Todo
import com.casa98.anotherlocalnotes.data.TodoRepository
import com.casa98.anotherlocalnotes.util.UIEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    savedStateHandle: SavedStateHandle  // Also magically works thanks to Hilt
): ViewModel() {

    var todo by mutableStateOf<Todo?>(null)
        private set

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    private val _uiEvent = Channel<UIEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()      // We can subscribe to it from UI

    init {
        // ViewModel used for adding new todos and editing them too
        val todoId = savedStateHandle.get<Int>("todoId")
        if(todoId != -1) {
            // Editing existing todo
            viewModelScope.launch {
                repository.getTodoById(todoId!!)?.let { todo ->
                    this@AddEditTodoViewModel.todo = todo
                    title = todo.title
                    description = todo.description ?: ""
                }
            }
        } else {
            // Adding new todo
        }
    }

    fun onEvent(event: AddEditTodoEvent) {
        when(event) {
            is AddEditTodoEvent.OnTitleChange -> {
                title = event.title
            }
            is AddEditTodoEvent.OnDescriptionChanged -> {
                description = event.description
            }
            is AddEditTodoEvent.OnSaveTodoClick -> {
                // Save todo
                viewModelScope.launch {
                    if(title.isBlank()) {
                        sendUIEvent(UIEvent.ShowSnackBar(
                            message = "The title can't be empty"
                        ))
                        return@launch
                    }
                    repository.insertTodo(
                        Todo(
                            id = todo?.id,      // If null, Room gill generate one
                            title = title,
                            description = description,
                            isDone = todo?.isDone ?: false,
                        )
                    )
                    sendUIEvent(UIEvent.PopBackStack)
                }
            }
        }
    }

    private fun sendUIEvent(event: UIEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}