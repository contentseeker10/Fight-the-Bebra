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
	
	START_GAME
}


func _ready() -> void:
	_connect_bridge()

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


#region Bridge to Java Client

func _process_bridge() -> void:
	var status := _update_bridge_connection()
	if status == StreamPeerSocket.Status.STATUS_CONNECTED:
		if bridge.get_available_bytes() > 0:
			_process_response()
	elif status == StreamPeerSocket.Status.STATUS_NONE:
		_connect_bridge()

func _process_response() -> void:
	var json := bridge.get_utf8_string(bridge.get_available_bytes())
	var data: Dictionary = JSON.parse_string(json)
	var command_type := _parse_command_str(data.get("command", "UNKNOWN"))
	var response: Dictionary = JSON.parse_string(data.get("response", {}))
	print(data)
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
			print("create lobby cmd")
			EventManager.create_lobby_completed.emit(success, error, response.get("code", "error"))
		
		CommandType.JOIN_LOBBY: pass
		
		CommandType.UPDATE_LOBBY: pass
		
		CommandType.LEAVE_LOBBY: pass



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
	return CommandType.UNKNOWN

func _init_timer(wait_time: float) -> Timer:
	var timer := Timer.new()
	timer.wait_time = wait_time
	timer.autostart = true
	add_child(timer)
	return timer

#endregion
