class_name Player
extends CharacterBody2D

@onready var sprite: AnimatedSprite2D = $Sprite
@onready var attack_area: Area2D = $AttackArea

@export var speed: float = 150.0

var is_dead: bool = false
var can_attack: bool = true

## Requests permission to perform an attack.
func request_attack() -> bool:
	return not is_dead and can_attack
