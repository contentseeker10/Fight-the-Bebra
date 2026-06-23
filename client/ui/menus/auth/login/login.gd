extends Control

@onready var error_label: Label = $ErrorLabel
@onready var username_edit: LineEdit = $CenterContainer/VBoxContainer/UsernameEdit
@onready var password_edit: LineEdit = $CenterContainer/VBoxContainer/PasswordEdit


func _ready() -> void:
	EventManager.login_completed.connect(_on_login_completed)

func _on_login_button_pressed() -> void:
	EventManager.login_requested.emit(username_edit.text, password_edit.text)

func _on_login_completed(success: bool, err_msg: String, user: User) -> void:
	if success:
		AccountManager.current_user = user
		get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
	else:
		error_label.text = err_msg
		error_label.show()
