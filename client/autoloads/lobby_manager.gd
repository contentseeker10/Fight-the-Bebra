extends Node

var code: String


func _ready() -> void:
	EventManager.create_lobby_requested.connect(_on_create_lobby_requested)
	EventManager.create_lobby_completed.connect(_on_create_lobby_completed)


func _on_create_lobby_requested() -> void:
	NetworkManager.send_request(NetworkManager.CommandType.CREATE_LOBBY, {})

func _on_create_lobby_completed(success: bool, error: String, lobby_code: String) -> void:
	if success:
		code = lobby_code
		get_tree().change_scene_to_file("res://ui/menus/lobby/room_create/room_create.tscn")
	else:
		printerr(error)
