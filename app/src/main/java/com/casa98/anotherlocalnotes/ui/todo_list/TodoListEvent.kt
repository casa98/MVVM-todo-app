package com.casa98.anotherlocalnotes.ui.todo_list

import com.casa98.anotherlocalnotes.data.Todo

sealed class TodoListEvent {
    // Use object if not params required
    data class OnDeleteTodoClick(val todo: Todo): TodoListEvent()
    data class OnDoneChange(val todo: Todo, val isDone: Boolean): TodoListEvent()
    object OnUndoDeleteClick: TodoListEvent()
    data class OnTodoClick(val todo: Todo): TodoListEvent()     // To view/edit it
    object OnAddTodoClick: TodoListEvent()
}