extends Control


func _on_create_room_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (ROOM CREATION)
	get_tree().change_scene_to_file("res://ui/menus/lobby/room_create.tscn")


func _on_join_room_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/lobby/room_join.tscn")
