extends Control

@onready var room_code: Label = $CenterContainer/VBoxContainer/RoomCode

func _ready() -> void:
	room_code.text = LobbyManager.code


func _on_start_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (GAME SESSION STARTED)
	get_tree().change_scene_to_file("res://parts/location/location.tscn")


func _on_exit_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (ROOM DISMISSED)
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
