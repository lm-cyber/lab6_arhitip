package com.alan.lab.server.utility.commands;

import com.alan.lab.common.network.Response;

public class NameHaventCommand extends Command {
    @Override
    public Response execute(Object argOrData, Long userID) {
        return new Response("havent command ,try help", false, true);
    }
}
