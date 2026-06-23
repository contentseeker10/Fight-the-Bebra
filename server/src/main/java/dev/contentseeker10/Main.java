package dev.contentseeker10;

import dev.contentseeker10.model.type.UserType;
import dev.contentseeker10.server.ServerManager;

public class Main {

    static void main() {
        ServerManager.getInstance().start(1, 1, 1, 1);
    }

}
