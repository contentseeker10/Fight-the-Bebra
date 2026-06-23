extends Control

@onready var room_code: Label = $CenterContainer/VBoxContainer/RoomCode
@onready var start_button: Button = $CenterContainer/VBoxContainer/VBoxContainer2/StartButton
@onready var player_count: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerCount
@onready var admin_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/AdminUsername
@onready var player_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerUsername


func _ready() -> void:
	_update_ui()


func _update_ui() -> void:
	room_code.text = LobbyManager.code
	
	if LobbyManager.admin:
		admin_username.text = LobbyManager.admin.username
	if LobbyManager.guest:
		player_username.text = LobbyManager.guest.username
	
	if AccountManager.current_user.type == User.UserType.ADMIN:
		start_button.disabled = false


func _on_start_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (GAME SESSION STARTED)
	get_tree().change_scene_to_file("res://parts/location/location.tscn")


func _on_exit_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (ROOM DISMISSED)
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
