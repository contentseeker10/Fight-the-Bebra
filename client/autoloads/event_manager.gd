extends Node


#region Registration & Authorization

@warning_ignore("unused_signal") signal register_requested(username: String, password: String)
@warning_ignore("unused_signal") signal login_requested(username: String, password: String)

@warning_ignore("unused_signal") signal register_completed(success: bool, err_msg: String)
@warning_ignore("unused_signal") signal login_completed(success: bool, err_msg: String, user: User)

#endregion


#region Network Statuses

@warning_ignore("unused_signal") signal bridge_status_updated(status: String, status_color: Color)
@warning_ignore("unused_signal") signal server_status_updated(status: String, status_color: Color)

#endregion
