package com.casa98.anotherlocalnotes.ui.todo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casa98.anotherlocalnotes.data.Todo
import com.casa98.anotherlocalnotes.data.TodoRepository
import com.casa98.anotherlocalnotes.util.Routes
import com.casa98.anotherlocalnotes.util.UIEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repository: TodoRepository
): ViewModel() {
    val todos = repository.getTodos()

    private val _uiEvent = Channel<UIEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()      // We can subscribe to it from UI

    private var deletedTodo: Todo? = null    // To cache recently deleted todo

    fun onEvent(event: TodoListEvent) {
        when(event) {
            is TodoListEvent.OnDeleteTodoClick   -> {
                viewModelScope.launch {
                    deletedTodo = event.todo
                    repository.deleteTodo(event.todo)
                    sendUIEvent(UIEvent.ShowSnackBar(
                        message = "Todo deleted",
                        action = "Undo"
                    ))
                }
            }
            is TodoListEvent.OnAddTodoClick -> {
                sendUIEvent(UIEvent.Navigate(Routes.ADD_EDIT_TODO))
            }
            is TodoListEvent.OnDoneChange -> {
                viewModelScope.launch {
                    repository.insertTodo(
                        event.todo.copy(
                            isDone = event.isDone   // We receive this value from checkbox UI
                        )
                    ) // Insert will replace the existing one
                }
            }
            is TodoListEvent.OnTodoClick -> {
                sendUIEvent(UIEvent.Navigate(Routes.ADD_EDIT_TODO + "?todoId=${event.todo.id}"))
            }
            is TodoListEvent.OnUndoDeleteClick -> {
                deletedTodo?.let { todo ->
                    // Reaches here if deletedTodo is not null
                    viewModelScope.launch {
                        repository.insertTodo(todo)
                    }
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