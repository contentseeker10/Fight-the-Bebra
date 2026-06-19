extends Control


func _on_register_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (REGISTER ACCOUNT)
	get_tree().change_scene_to_file("res://ui/menus/auth/login.tscn")
