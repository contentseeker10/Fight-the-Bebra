extends Node

var code: String

var admin: User
var guest: User


func _ready() -> void:
	EventManager.create_lobby_requested.connect(_on_create_lobby_requested)
	EventManager.create_lobby_completed.connect(_on_create_lobby_completed)
	
	EventManager.join_lobby_requested.connect(_on_join_lobby_requested)
	EventManager.join_lobby_completed.connect(_on_join_lobby_completed)
	
	EventManager.update_lobby_completed.connect(_on_update_lobby_completed)
	
	EventManager.leave_lobby_requested.connect(_on_leave_lobby_requested)
	EventManager.leave_lobby_completed.connect(_on_leave_lobby_completed)


func _on_create_lobby_requested() -> void:
	NetworkManager.send_request(NetworkManager.CommandType.CREATE_LOBBY, {})

func _on_create_lobby_completed(success: bool, error: String, lobby_code: String) -> void:
	if success:
		code = lobby_code
		
		# WARNING: Only for UI purposes, access control is still onto server
		AccountManager.current_user.type = User.UserType.ADMIN
		admin = AccountManager.current_user
		
		get_tree().change_scene_to_file("res://ui/menus/lobby/room_create/room_create.tscn")
	else:
		printerr(error)


func _on_join_lobby_requested(lobby_code: String) -> void:
	NetworkManager.send_request(NetworkManager.CommandType.JOIN_LOBBY, { "lobbyCode": lobby_code })

func _on_join_lobby_completed(success: bool, error: String, lobby_admin: User) -> void:
	if success:
		# WARNING: Only for UI purposes, access control is still onto server
		AccountManager.current_user.type = User.UserType.GUEST
		admin = lobby_admin
		guest = AccountManager.current_user
		get_tree().change_scene_to_file("res://ui/menus/lobby/room_create/room_create.tscn")
	else:
		printerr(error)


func _on_update_lobby_completed(success: bool, error: String, users: Array) -> void:
	if success:
		if not users.is_empty():
			guest = User.new(users[0])
			EventManager.guest_joined.emit(guest)
		else:
			# WARNING: Only for UI purposes, access control is still onto server
			if AccountManager.current_user.type == User.UserType.GUEST:
				AccountManager.current_user.type = User.UserType.ADMIN
				guest = null
				admin = AccountManager.current_user
			EventManager.player_left.emit()
	else:
		printerr(error)


func _on_leave_lobby_requested(_lobby_code: String) -> void:
	NetworkManager.send_request(NetworkManager.CommandType.LEAVE_LOBBY, { "lobbyCode": code })

func _on_leave_lobby_completed(success: bool, error: String) -> void:
	if success:
		get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
	else:
		printerr(error)
