extends CanvasLayer

@onready var bridge_status: Label = $HBoxContainer/BridgeStatus
@onready var server_status: Label = $HBoxContainer/ServerStatus


func _ready() -> void:
	EventManager.bridge_status_updated.connect(_on_bridge_status_updated)


func _on_bridge_status_updated(status: String, status_color: Color) -> void:
	bridge_status.text = "Bridge status: " + status
	bridge_status.add_theme_color_override("font_color", status_color)
