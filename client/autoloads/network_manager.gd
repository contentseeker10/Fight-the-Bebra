extends Node

const HOST := "127.0.0.1"
const PORT := 9000

var bridge: StreamPeerTCP = StreamPeerTCP.new()


func _ready() -> void:
	EventManager.register_requested.connect(_on_register_requested)
	EventManager.login_requested.connect(_on_login_requested)
	
	_connect_bridge()
	await _update_connection(0.5)

func _connect_bridge() -> void:
	var err := bridge.connect_to_host(HOST, PORT)
	if err != OK:
		printerr("Error connecting to Java Client localhost: ", HOST, ":", PORT)


func _process(delta: float) -> void:
	pass

func _update_connection(delay: float) -> void:
	var timer := _init_timer(delay)
	while true:
		await timer.timeout
		bridge.poll()
		var status: String
		var status_color: String
		match bridge.get_status():
			StreamPeerSocket.Status.STATUS_NONE: 
				status = "NONE"
				status_color = "gray"
			StreamPeerSocket.Status.STATUS_CONNECTING: 
				status = "CONNECTING"
				status_color = "orange"
			StreamPeerSocket.Status.STATUS_CONNECTED: 
				status = "CONNECTED"
				status_color = "green"
			StreamPeerSocket.Status.STATUS_ERROR: 
				status = "ERROR"
				status_color = "red"
		print_rich("[color=", status_color, "]TCP Connection Status: ", status, "[/color]")


#region Registration & Authorization

func _on_register_requested(username: String, password: String) -> void:
	pass

func _on_login_requested(username: String, password: String) -> void:
	pass

#endregion


#region Utility

func _init_timer(wait_time: float) -> Timer:
	var timer := Timer.new()
	timer.wait_time = wait_time
	timer.autostart = true
	add_child(timer)
	return timer

#endregion
