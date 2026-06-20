extends Node

const HOST := "127.0.0.1"
const PORT := 9000

var bridge := StreamPeerTCP.new()


func _ready() -> void:
	EventManager.register_requested.connect(_on_register_requested)
	EventManager.login_requested.connect(_on_login_requested)
	
	_connect_bridge()

func _connect_bridge() -> void:
	var err := bridge.connect_to_host(HOST, PORT)
	if err != OK:
		printerr("Error connecting to Java Bridge: ", HOST, ":", PORT)


func _process(_delta: float) -> void:
	_process_bridge()


#region Bridge to Java Client

func _process_bridge() -> void:
	var status := _update_bridge_connection()
	if status == StreamPeerSocket.Status.STATUS_CONNECTED:
		if bridge.get_available_bytes() > 0:
			var json := bridge.get_utf8_string(bridge.get_available_bytes())
			var data: Dictionary = JSON.parse_string(json)
			
			# TODO: Make more Generic way of processing incoming data
			# Now it's just auth
			
			if data.has("user"):
				var user_data: Variant = data.get("user")
				var user: User
				if user_data:
					user = User.new(user_data)
				EventManager.login_completed.emit(
					data.get("success", false), 
					data.get("error", ""),
					user
					)
			else:
				EventManager.register_completed.emit(
					data.get("success", false), 
					data.get("error", "")
					)
	elif status == StreamPeerSocket.Status.STATUS_NONE:
		_connect_bridge()

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


#region Registration & Authorization

func _on_register_requested(username: String, password: String) -> void:
	var request := {
		"command": "REGISTER",
		"request": { "username": username, "password": password }
	}
	var json := JSON.stringify(request) + "\n"
	bridge.put_data(json.to_utf8_buffer())

func _on_login_requested(username: String, password: String) -> void:
	var request := {
		"command": "LOGIN",
		"request": { "username": username, "password": password }
	}
	var json := JSON.stringify(request) + "\n"
	bridge.put_data(json.to_utf8_buffer())

#endregion


#region Utility

func _init_timer(wait_time: float) -> Timer:
	var timer := Timer.new()
	timer.wait_time = wait_time
	timer.autostart = true
	add_child(timer)
	return timer

#endregion
