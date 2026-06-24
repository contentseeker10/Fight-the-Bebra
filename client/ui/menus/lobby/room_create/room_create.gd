extends Control

@onready var room_code: Label = $CenterContainer/VBoxContainer/RoomCode
@onready var start_button: Button = $CenterContainer/VBoxContainer/VBoxContainer2/StartButton
@onready var player_count: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerCount
@onready var admin_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/AdminUsername
@onready var player_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerUsername


func _ready() -> void:
	EventManager.guest_joined.connect(_on_guest_joined)
	EventManager.player_left.connect(_on_player_left)
	_update_ui()


func _update_ui() -> void:
	room_code.text = LobbyManager.code
	
	admin_username.text = LobbyManager.admin.username
	
	if LobbyManager.guest:
		player_count.text = "Players 2/2"
		player_username.show()
		player_username.text = LobbyManager.guest.username
	else:
		player_count.text = "Players 1/2"
		player_username.hide()
	
	if AccountManager.current_user.type == User.UserType.ADMIN:
		start_button.disabled = false


func _on_start_button_pressed() -> void:
	start_button.disabled = true
	NetworkManager.send_request(NetworkManager.CommandType.START_GAME, { "lobbyCode": LobbyManager.code })


func _on_exit_button_pressed() -> void:
	EventManager.leave_lobby_requested.emit(LobbyManager.code)
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")


func _on_guest_joined(_guest: User) -> void:
	_update_ui()


func _on_player_left() -> void:
	_update_ui()
