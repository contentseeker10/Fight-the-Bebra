extends Control


func _on_login_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (LOG IN TO ACCOUNT)
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
