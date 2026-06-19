extends Control


func _on_join_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (JOINING ROOM)
	pass


func _on_return_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/lobby/lobby.tscn")
