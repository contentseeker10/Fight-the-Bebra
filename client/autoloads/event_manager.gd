extends Node


#region Authorization

@warning_ignore("unused_signal") signal register_requested(username: String, password: String)
@warning_ignore("unused_signal") signal login_requested(username: String, password: String)

@warning_ignore("unused_signal") signal register_completed(success: bool, err_msg: String)
@warning_ignore("unused_signal") signal login_completed(success: bool, err_msg: String, user: User)

#endregion


#region Lobby

@warning_ignore("unused_signal") signal create_lobby_requested()
@warning_ignore("unused_signal") signal create_lobby_completed(success: bool, error: String, lobby_code: String)

@warning_ignore("unused_signal") signal join_lobby_requested(lobby_code: String)
@warning_ignore("unused_signal") signal join_lobby_completed(success: bool, error: String, lobby_admin: User)

@warning_ignore("unused_signal") signal update_lobby_requested()
@warning_ignore("unused_signal") signal update_lobby_completed(success: bool, error: String, users: Array)
@warning_ignore("unused_signal") signal guest_joined(guest: User)
@warning_ignore("unused_signal") signal player_left()

@warning_ignore("unused_signal") signal leave_lobby_requested(lobby_code: String)
@warning_ignore("unused_signal") signal leave_lobby_completed(success: bool, error: String)

#endregion


#region Network Statuses

@warning_ignore("unused_signal") signal bridge_status_updated(status: String, status_color: Color)
@warning_ignore("unused_signal") signal server_status_updated(status: String, status_color: Color)

#endregion
