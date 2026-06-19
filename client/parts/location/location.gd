extends Node2D

var _player_scene: PackedScene = preload("res://parts/player/player.tscn")


func _ready() -> void:
	_init_player()

func _init_player() -> void:
	var player: Player = _player_scene.instantiate()
	add_child(player)
