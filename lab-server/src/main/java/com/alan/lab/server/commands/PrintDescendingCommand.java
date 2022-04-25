package com.alan.lab.server.commands;


import com.alan.lab.server.utility.CollectionManager;


public class PrintDescendingCommand extends Command {

    private final CollectionManager collectionManager;

    public PrintDescendingCommand(CollectionManager collectionManager) {
        super("print_descending");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResult execute(String arg) {
        return new CommandResult(false, collectionManager.descending().toString());
    }
}
