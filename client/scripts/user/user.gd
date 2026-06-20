class_name User
extends Node

var id: int
var username: String
var record_score: int
var death_count: int


func _init(user_data: Dictionary) -> void:
	id = user_data.get("id", -1)
	username = user_data.get("username", "ERROR")
	record_score = user_data.get("recordScore", -1)
	death_count = user_data.get("deathCount", -1)
