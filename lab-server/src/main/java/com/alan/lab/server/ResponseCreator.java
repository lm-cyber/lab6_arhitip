package com.alan.lab.server;

import com.alan.lab.common.data.Person;
import com.alan.lab.common.network.RequestWithPersonType;
import com.alan.lab.common.network.Response;
import com.alan.lab.server.utility.collectionmanagers.CollectionManager;
import com.alan.lab.server.utility.HistoryManager;
import com.alan.lab.server.utility.commands.AddCom;
import com.alan.lab.server.utility.commands.AddCommand;
import com.alan.lab.server.utility.commands.AddIfMinCommand;
import com.alan.lab.server.utility.commands.AverageOfHeightCommand;
import com.alan.lab.server.utility.commands.ClearCommand;
import com.alan.lab.server.utility.commands.Command;
import com.alan.lab.server.utility.commands.CommandWithPerson;
import com.alan.lab.server.utility.commands.FilterGreaterThanHeightCommand;
import com.alan.lab.server.utility.commands.HelpCommad;
import com.alan.lab.server.utility.commands.HistoryCommand;
import com.alan.lab.server.utility.commands.InfoCommand;
import com.alan.lab.server.utility.commands.NameHaventCommand;
import com.alan.lab.server.utility.commands.PrintDescendingCommand;
import com.alan.lab.server.utility.commands.RemoveByIdCommand;
import com.alan.lab.server.utility.commands.RemoveHeadCommand;
import com.alan.lab.server.utility.commands.ShowCommand;
import com.alan.lab.server.utility.commands.UpdateCom;
import com.alan.lab.server.utility.commands.UpdateCommand;

import java.util.HashMap;

public class ResponseCreator {
    private final HistoryManager historyManager;
    private final CollectionManager collectionManager;
    private final HashMap<String, Command> commands;
    private final HashMap<RequestWithPersonType, CommandWithPerson> typeCommand;
    private Long lastId;

    public ResponseCreator(HistoryManager historyManager, CollectionManager collectionManager) {
        this.historyManager = historyManager;
        this.collectionManager = collectionManager;
        this.commands = new HashMap<>();
        this.typeCommand = new HashMap<>();

        typeCommand.put(RequestWithPersonType.ADD, new AddCommand(collectionManager));
        typeCommand.put(RequestWithPersonType.UPDATE, new UpdateCommand(collectionManager));
        typeCommand.put(RequestWithPersonType.ADD_IF_MIN, new AddIfMinCommand(collectionManager));
        commands.put("clear", new ClearCommand(collectionManager));
        commands.put("help", new HelpCommad());
        commands.put("history", new HistoryCommand(historyManager));
        commands.put("show", new ShowCommand(collectionManager));
        commands.put("info", new InfoCommand(collectionManager));
        commands.put("average_of_height", new AverageOfHeightCommand(collectionManager));
        commands.put("filter_greater_than_height", new FilterGreaterThanHeightCommand(collectionManager));
        commands.put("print_descending", new PrintDescendingCommand(collectionManager));
        commands.put("remove_by_id", new RemoveByIdCommand(collectionManager));
        commands.put("remove_head", new RemoveHeadCommand(collectionManager));
        commands.put("add", new AddCom());
        commands.put("update", new UpdateCom(collectionManager));
        commands.put("add_if_min", new AddCom());
    }

    public void addHistory(String input) {
        historyManager.addNote(input);
    }

    public Long getLastId() {
        return lastId;
    }


    public Response executeCommand(String name, Object arg) {
        if (arg instanceof Long) {
            lastId = (Long) arg;
        }
        return commands.getOrDefault(name, new NameHaventCommand()).execute(arg);
    }


    public Response executeCommandWithPerson(RequestWithPersonType type, Person person) {
        return typeCommand.get(type).execute(person, lastId);
    }
}
