package com.alan.lab.server;


import com.alan.lab.common.data.Person;
import com.alan.lab.common.network.Request;
import com.alan.lab.common.network.RequestWithPerson;
import com.alan.lab.common.network.Response;
import com.alan.lab.common.utility.nonstandardcommand.NonStandardCommand;
import com.alan.lab.server.utility.NonStandardCommandServer;
import com.alan.lab.server.utility.collectionmanagers.CollectionManager;
import com.alan.lab.server.utility.collectionmanagers.FileManager;
import com.alan.lab.server.utility.HistoryManager;
import com.alan.lab.server.utility.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ServerInstance {

    private static final int TIMEOUTWRITE = 100;
    private static final int SOCKET_TIMEOUT = 10;
    private final ResponseCreator responseCreator;
    private final FileManager fileManager;

    private final CollectionManager collectionManager;
    private final HashSet<ObjectSocketWrapper> clients;
    private final Logger logger;
    private final NonStandardCommand nonStandardCommandServer;

    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public ServerInstance(String fileName) {
        this.collectionManager = new CollectionManager();
        this.fileManager = new FileManager(fileName);
        this.responseCreator = new ResponseCreator(new HistoryManager(), collectionManager);
        clients = new HashSet<>();
        this.logger = Logger.getLogger("log");
        this.nonStandardCommandServer = new NonStandardCommandServer(collectionManager, logger, in, fileManager);
        File lf = new File("server.log");
        FileHandler fh = null;
        try {
            fh = new FileHandler(lf.getAbsolutePath(), true);
            logger.addHandler(fh);
        } catch (IOException e) {
            System.out.println(e.getMessage() + "logger not write in file");
        }
    }

    private void start() throws FileNotFoundException {
        StringBuilder stringData = null;
        PriorityQueue<Person> people;
        stringData = fileManager.read();
        people = new PriorityQueue<>(JsonParser.toData(String.valueOf(stringData)));

        collectionManager.initialiseData(people);
    }


    public void handleRequests() throws IOException {
        Iterator<ObjectSocketWrapper> it = clients.iterator();
        while (it.hasNext()) {
            ObjectSocketWrapper client = it.next();

            try {
                if (client.checkForMessage()) {
                    Object received = client.getPayload();
                    logger.info("get Payload");
                    if (received instanceof RequestWithPerson) {
                        logger.info("request with person");
                        RequestWithPerson requestWithPerson = (RequestWithPerson) received;
                        Response response = responseCreator.executeCommandWithPerson(requestWithPerson.getType(), requestWithPerson.getPerson());
                        client.sendMessage(response);
                    } else if (received instanceof Request) {
                        Request request = (Request) received;
                        responseCreator.addHistory(request.getCommandName() + " " + request.getArgs().toString());
                        logger.info("doing " + request.getCommandName() + " " + request.getArgs().toString());
                        Response response = responseCreator.executeCommand(request.getCommandName(), request.getArgs());
                        client.sendMessage(response);
                        logger.fine("send message");

                    }


                    client.clearInBuffer();
                }
            } catch (IOException e) {
                client.getSocket().close();
                it.remove();
            }
        }
    }

    public void run(int port) throws IOException {
        int check = 0;
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            start();
            logger.info("Server is listening on port " + port);
            while (true) {
                if (nonStandardCommandServer.execute(null)) {
                    return;
                }
                try {
                    while (true) {
                        Socket newClient = socket.accept();
                        newClient.setSoTimeout(SOCKET_TIMEOUT);
                        logger.info("Received connection from " + newClient.getRemoteSocketAddress());
                        clients.add(new ObjectSocketWrapper(newClient));
                    }
                } catch (SocketTimeoutException e) {
                    if (check++ >= TIMEOUTWRITE) {
                        check = 0;
                        logger.info("time out");
                    }
                }
                handleRequests();
            }
        }
    }
}
