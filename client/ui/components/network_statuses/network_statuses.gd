extends CanvasLayer

@onready var bridge_status: Label = $HBoxContainer/BridgeStatus
@onready var server_status: Label = $HBoxContainer/ServerStatus


func _ready() -> void:
	EventManager.bridge_status_updated.connect(_on_bridge_status_updated)
	EventManager.server_status_updated.connect(_on_server_status_updated)


func _on_bridge_status_updated(status: String, status_color: Color) -> void:
	bridge_status.text = "Bridge: " + status
	bridge_status.add_theme_color_override("font_color", status_color)


func _on_server_status_updated(status: String, status_color: Color) -> void:
	server_status.text = "Server: " + status
	server_status.add_theme_color_override("font_color", status_color)
