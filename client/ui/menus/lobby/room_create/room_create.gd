extends Control

@onready var room_code: Label = $CenterContainer/VBoxContainer/RoomCode
@onready var start_button: Button = $CenterContainer/VBoxContainer/VBoxContainer2/StartButton
@onready var player_count: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerCount
@onready var admin_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/AdminUsername
@onready var player_username: Label = $CenterContainer/VBoxContainer/VBoxContainer/PlayerUsername

@onready var chat_input: LineEdit = $ChatContainer/VBoxContainer/LineEdit
@onready var messages_container: VBoxContainer = $ChatContainer/VBoxContainer/ScrollContainer/MarginContainer/VBoxContainer
@onready var scroll_container: ScrollContainer = $ChatContainer/VBoxContainer/ScrollContainer

var _left_message_template: RichTextLabel
var _right_message_template: RichTextLabel

func _ready() -> void:
	EventManager.guest_joined.connect(_on_guest_joined)
	EventManager.player_left.connect(_on_player_left)
	
	# Setup chat templates and clear mock messages
	_left_message_template = messages_container.get_node("LeftMessage").duplicate()
	_right_message_template = messages_container.get_node("RightMessage").duplicate()
	
	for child in messages_container.get_children():
		child.queue_free()
		
	chat_input.text_submitted.connect(_on_chat_input_submitted)
	EventManager.chat_message_received.connect(_on_chat_message_received)
	
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


func _on_chat_input_submitted(text: String) -> void:
	var msg := text.strip_edges()
	if msg.is_empty():
		return
	NetworkManager.send_request(NetworkManager.CommandType.SEND_MSG, { "message": msg })
	chat_input.clear()


func _on_chat_message_received(sender: String, message: String) -> void:
	var msg_node: RichTextLabel
	if sender == AccountManager.current_user.username:
		msg_node = _right_message_template.duplicate()
	else:
		msg_node = _left_message_template.duplicate()
		
	msg_node.text = "[b]" + sender + ":[/b]\n" + message
	messages_container.add_child(msg_node)
	
	# Scroll to bottom
	await get_tree().process_frame
	scroll_container.scroll_vertical = scroll_container.get_v_scroll_bar().max_value
