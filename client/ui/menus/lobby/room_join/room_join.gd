extends Control

@onready var room_code_edit: LineEdit = $CenterContainer/VBoxContainer/RoomIdEdit


func _on_join_button_pressed() -> void:
	EventManager.join_lobby_requested.emit(room_code_edit.text)


func _on_return_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
