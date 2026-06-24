extends Node2D

var _player_scene: PackedScene = preload("res://parts/player/player.tscn")

var local_player: Player
var teammate: Player
var teammate_prev_pos: Vector2 = Vector2.ZERO
var teammate_dir: String = "down"
var teammate_was_attacking: bool = false
var send_timer: Timer

func _ready() -> void:
	_init_players()
	EventManager.teammate_state_updated.connect(_on_teammate_state_updated)
	
	send_timer = Timer.new()
	send_timer.wait_time = 0.033 # ~30 FPS
	send_timer.autostart = true
	send_timer.timeout.connect(_send_local_state)
	add_child(send_timer)

func _init_players() -> void:
	# Local player
	local_player = _player_scene.instantiate()
	local_player.is_remote = false
	add_child(local_player)
	
	# Remote teammate
	teammate = _player_scene.instantiate()
	teammate.is_remote = true
	teammate.modulate = Color(0.7, 0.8, 1.0, 1.0) # slightly blue tinted
	add_child(teammate)
	
	# Connect teammate animations
	teammate.sprite.animation_finished.connect(_on_teammate_animation_finished)
	
	# Spawn points
	if LobbyManager.admin and AccountManager.current_user.id == LobbyManager.admin.id:
		local_player.global_position = Vector2(100, 150)
		teammate.global_position = Vector2(200, 150)
	else:
		local_player.global_position = Vector2(200, 150)
		teammate.global_position = Vector2(100, 150)
		
	teammate_prev_pos = teammate.global_position

func _send_local_state() -> void:
	if not local_player:
		return
		
	var is_attacking := false
	if local_player.sprite.animation.begins_with("attack"):
		is_attacking = true
		
	NetworkManager.send_request(NetworkManager.CommandType.GAME_INPUT, {
		"x": local_player.global_position.x,
		"y": local_player.global_position.y,
		"hp": 100,
		"isAttacking": is_attacking
	})

func _on_teammate_state_updated(x: float, y: float, _hp: int, is_attacking: bool) -> void:
	if not teammate:
		return
		
	var new_pos := Vector2(x, y)
	var diff := new_pos - teammate.global_position
	teammate.global_position = new_pos
	
	# Determine direction of teammate based on movement
	if diff.length() > 2.0:
		if abs(diff.x) > abs(diff.y):
			teammate_dir = "right" if diff.x > 0 else "left"
		else:
			teammate_dir = "down" if diff.y > 0 else "up"
		
		if not is_attacking and not teammate.sprite.animation.begins_with("attack"):
			var anim = "run_" + teammate_dir
			if teammate.sprite.animation != anim or not teammate.sprite.is_playing():
				teammate.sprite.play(anim)
	else:
		if not is_attacking and not teammate.sprite.animation.begins_with("attack"):
			var anim = "idle_" + teammate_dir
			if teammate.sprite.animation != anim or not teammate.sprite.is_playing():
				teammate.sprite.play(anim)
			
	if is_attacking and not teammate_was_attacking:
		var anim = "attack_1_" + teammate_dir
		teammate.sprite.play(anim)
		var sword_sfx = preload("res://assets/audio/player/Sword Attack 1.wav")
		AudioManager.play_sfx_2d(sword_sfx, teammate.global_position, 0.08, "SFX")
		
	teammate_was_attacking = is_attacking

func _on_teammate_animation_finished() -> void:
	if teammate and teammate.sprite.animation.begins_with("attack"):
		var current_anim = teammate.sprite.animation
		if current_anim.begins_with("attack_1_"):
			var dir = current_anim.replace("attack_1_", "")
			teammate.sprite.play("attack_2_" + dir)
			var sword_sfx2 = preload("res://assets/audio/player/Sword Attack 2.wav")
			AudioManager.play_sfx_2d(sword_sfx2, teammate.global_position, 0.08, "SFX")
		else:
			teammate.sprite.play("idle_" + teammate_dir)
