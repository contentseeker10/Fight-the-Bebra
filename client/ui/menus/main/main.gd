extends Control


func _on_login_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/auth/login/login.tscn")


func _on_register_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/auth/registration/registration.tscn")
