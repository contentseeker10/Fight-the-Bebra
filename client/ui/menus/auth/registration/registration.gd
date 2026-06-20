extends Control

@onready var error_label: Label = $ErrorLabel
@onready var username_edit: LineEdit = $CenterContainer/VBoxContainer/UsernameEdit
@onready var password_edit: LineEdit = $CenterContainer/VBoxContainer/PasswordEdit


func _ready() -> void:
	EventManager.register_completed.connect(_on_register_completed)

func _on_register_button_pressed() -> void:
	EventManager.register_requested.emit(username_edit.text, password_edit.text)

func _on_register_completed(success: bool, error: String) -> void:
	if success:
		get_tree().change_scene_to_file("res://ui/menus/auth/login/login.tscn")
	else:
		error_label.text = error
		error_label.show()
