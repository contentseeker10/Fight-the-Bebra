class_name User
extends Node

enum UserType {
	NONE,
	GUEST,
	ADMIN
}

var id: int
var type: UserType
var username: String
var record_score: int
var death_count: int


func _init(user_data: Dictionary) -> void:
	id = user_data.get("id", -1)
	type = _str_to_user_type(user_data.get("type", "NONE"))
	username = user_data.get("username", "ERROR")
	record_score = user_data.get("recordScore", -1)
	death_count = user_data.get("deathCount", -1)


func _str_to_user_type(user_type: String) -> UserType:
	match user_type:
		"GUEST": return UserType.GUEST
		"ADMIN": return UserType.ADMIN
	return UserType.NONE
