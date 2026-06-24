extends Node

const HOST := "127.0.0.1"
const PORT := 9000

var bridge := StreamPeerTCP.new()

enum CommandType {
	UNKNOWN,
	RESPONSE,
	
	REGISTER,
	LOGIN,
	
	CREATE_LOBBY,
	JOIN_LOBBY,
	UPDATE_LOBBY,
	LEAVE_LOBBY,
	
	START_GAME,
	
	HANDSHAKE,
	GAME_INPUT,
	GAME_STATE,
	
	SERVER_STATUS,
	SEND_MSG,
	CHAT_MSG
}


func _ready() -> void:
	_connect_bridge()
	EventManager.server_status_updated.emit("Connection lost", Color.RED)

func _connect_bridge() -> void:
	var err := bridge.connect_to_host(HOST, PORT)
	if err != OK:
		printerr("Error connecting to Java Bridge: ", HOST, ":", PORT)


func _process(_delta: float) -> void:
	_process_bridge()


#region Main functions

func send_request(command: CommandType, request: Dictionary) -> void:
	var cmd := _parse_command(command)
	var message := {
		"command": cmd,
		"request": request
	}
	var json := JSON.stringify(message) + "\n"
	bridge.put_data(json.to_utf8_buffer())

#endregion


var _receive_buffer: String = ""


#region Bridge to Java Client

func _process_bridge() -> void:
	var status := _update_bridge_connection()
	if status == StreamPeerSocket.Status.STATUS_CONNECTED:
		if bridge.get_available_bytes() > 0:
			_process_response()
	elif status == StreamPeerSocket.Status.STATUS_NONE:
		_connect_bridge()

func _process_response() -> void:
	var chunk := bridge.get_utf8_string(bridge.get_available_bytes())
	_receive_buffer += chunk
	
	while "\n" in _receive_buffer:
		var parts := _receive_buffer.split("\n", true, 1)
		var line := parts[0].strip_edges()
		_receive_buffer = parts[1] if parts.size() > 1 else ""
		
		if line.is_empty():
			continue
			
		var data = JSON.parse_string(line)
		if typeof(data) != TYPE_DICTIONARY:
			continue
			
		_handle_single_response(data)

func _handle_single_response(data: Dictionary) -> void:
	var command_type := _parse_command_str(data.get("command", "UNKNOWN"))
	var resp_node = data.get("response", {})
	var response: Dictionary
	if typeof(resp_node) == TYPE_STRING:
		response = JSON.parse_string(resp_node)
	elif typeof(resp_node) == TYPE_DICTIONARY:
		response = resp_node
	else:
		response = {}
		
	print("Received from bridge: ", command_type, " -> ", response)
	if response.is_empty() or command_type == CommandType.UNKNOWN:
		printerr("Bad Response: ", data)
		return
	var success: bool = response.get("success", false)
	var error: String = response.get("error", "error")
	match command_type:
		CommandType.RESPONSE: pass
		
		CommandType.REGISTER:
			EventManager.register_completed.emit(success, error)
		
		CommandType.LOGIN:
			var user: User
			if success:
				user = User.new(response.get("user", {}))
			EventManager.login_completed.emit(success, error, user)
		
		CommandType.CREATE_LOBBY:
			EventManager.create_lobby_completed.emit(success, error, response.get("code", "error"))
		
		CommandType.JOIN_LOBBY:
			var admin: User
			if success: 
				admin = User.new(response.get("admin", {}))
			EventManager.join_lobby_completed.emit(success, error, admin)
		
		CommandType.UPDATE_LOBBY: 
			if not success:
				printerr("Error updating lobby")
				return
			var users: Array = response.get("users", [])
			EventManager.update_lobby_completed.emit(success, error, users)
		
		CommandType.LEAVE_LOBBY: 
			EventManager.leave_lobby_completed.emit(success, error)
			
		CommandType.START_GAME:
			var udp_token: String = response.get("udpToken", "")
			var udp_port: int = response.get("udpPort", 9091)
			EventManager.match_started.emit(success, error, udp_token, udp_port)
			
		CommandType.HANDSHAKE:
			if success:
				print("[NETWORK] UDP Handshake successful!")
				EventManager.server_status_updated.emit("UDP: Connected", Color.WEB_GREEN)
			else:
				printerr("[NETWORK] UDP Handshake failed: ", error)
				EventManager.server_status_updated.emit("UDP: Disconnected", Color.RED)
				
		CommandType.GAME_STATE:
			var user_id: int = response.get("userId", 0)
			if user_id != AccountManager.current_user.id:
				var x: float = response.get("x", 0.0)
				var y: float = response.get("y", 0.0)
				var hp: int = response.get("hp", 100)
				var is_attacking: bool = response.get("isAttacking", false)
				EventManager.teammate_state_updated.emit(x, y, hp, is_attacking)
				
		CommandType.SERVER_STATUS:
			var status: String = response.get("status", "CONNECTION_LOST")
			if status == "CONNECTED":
				EventManager.server_status_updated.emit("TCP: Connected", Color.WEB_GREEN)
			else:
				EventManager.server_status_updated.emit("Connection lost", Color.RED)
				
		CommandType.CHAT_MSG:
			var sender: String = response.get("sender", "unknown")
			var message: String = response.get("message", "")
			EventManager.chat_message_received.emit(sender, message)



func _update_bridge_connection() -> StreamPeerSocket.Status:
	bridge.poll()
	
	var status: String
	var status_color: Color
	
	match bridge.get_status():
		StreamPeerSocket.Status.STATUS_NONE: 
			status = "NONE"
			status_color = Color.GRAY
		StreamPeerSocket.Status.STATUS_CONNECTING: 
			status = "CONNECTING"
			status_color = Color.ORANGE
		StreamPeerSocket.Status.STATUS_CONNECTED: 
			status = "CONNECTED"
			status_color = Color.WEB_GREEN
		StreamPeerSocket.Status.STATUS_ERROR: 
			status = "ERROR"
			status_color = Color.RED
	
	EventManager.bridge_status_updated.emit(status, status_color)
	
	return bridge.get_status()

#endregion


#region Utility

func _parse_command(command: CommandType) -> String:
	match command:
		CommandType.RESPONSE: return "RESPONSE"
		CommandType.REGISTER: return "REGISTER"
		CommandType.LOGIN: return "LOGIN"
		CommandType.CREATE_LOBBY: return "CREATE_LOBBY"
		CommandType.JOIN_LOBBY: return "JOIN_LOBBY"
		CommandType.UPDATE_LOBBY: return "UPDATE_LOBBY"
		CommandType.LEAVE_LOBBY: return "LEAVE_LOBBY"
		CommandType.START_GAME: return "START_GAME"
		CommandType.HANDSHAKE: return "HANDSHAKE"
		CommandType.GAME_INPUT: return "GAME_INPUT"
		CommandType.GAME_STATE: return "GAME_STATE"
		CommandType.SERVER_STATUS: return "SERVER_STATUS"
		CommandType.SEND_MSG: return "SEND_MSG"
		CommandType.CHAT_MSG: return "CHAT_MSG"
	return "UNKNOWN"

func _parse_command_str(command: String) -> CommandType:
	match command:
		"RESPONSE": return CommandType.RESPONSE
		"REGISTER": return CommandType.REGISTER
		"LOGIN": return CommandType.LOGIN 
		"CREATE_LOBBY": return CommandType.CREATE_LOBBY
		"JOIN_LOBBY": return CommandType.JOIN_LOBBY
		"UPDATE_LOBBY": return CommandType.UPDATE_LOBBY
		"LEAVE_LOBBY": return CommandType.LEAVE_LOBBY
		"START_GAME": return CommandType.START_GAME
		"HANDSHAKE": return CommandType.HANDSHAKE
		"GAME_INPUT": return CommandType.GAME_INPUT
		"GAME_STATE": return CommandType.GAME_STATE
		"SERVER_STATUS": return CommandType.SERVER_STATUS
		"SEND_MSG": return CommandType.SEND_MSG
		"CHAT_MSG": return CommandType.CHAT_MSG
	return CommandType.UNKNOWN

func _init_timer(wait_time: float) -> Timer:
	var timer := Timer.new()
	timer.wait_time = wait_time
	timer.autostart = true
	add_child(timer)
	return timer

#endregion
