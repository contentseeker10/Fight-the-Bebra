extends Node


func _ready() -> void:
	EventManager.register_requested.connect(_on_register_requested)
	EventManager.login_requested.connect(_on_login_requested)


func _on_register_requested(username: String, password: String) -> void:
	var request := {
		"username": username, 
		"password": password 
	}
	NetworkManager.send_request(NetworkManager.CommandType.REGISTER, request)


func _on_login_requested(username: String, password: String) -> void:
	var request := {
		"username": username, 
		"password": password 
	}
	NetworkManager.send_request(NetworkManager.CommandType.LOGIN, request)
