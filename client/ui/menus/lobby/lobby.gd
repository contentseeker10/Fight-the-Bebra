extends Control

@onready var username: Label = $Username
@onready var record_score: Label = $MarginContainer/VBoxContainer/RecordScore
@onready var death_count: Label = $MarginContainer/VBoxContainer/DeathCount


func _ready() -> void:
	_update_user_data()

func _update_user_data() -> void:
	username.text = AccountManager.current_user.username
	record_score.text = "record score: " + str(AccountManager.current_user.record_score)
	death_count.text = "death count: " + str(AccountManager.current_user.death_count)


func _on_create_room_button_pressed() -> void:
	# TODO: Call to Java client over NetworkManager (ROOM CREATION)
	get_tree().change_scene_to_file("res://ui/menus/lobby/room_create/room_create.tscn")


func _on_join_room_button_pressed() -> void:
	get_tree().change_scene_to_file("res://ui/menus/lobby/room_join/room_join.tscn")
