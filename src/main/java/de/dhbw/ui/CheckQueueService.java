package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Message;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class CheckQueueService extends ScheduledService<List<Message>> {
    private  final List<Message> messages = new ArrayList<>(10);

    @Override
    protected Task<List<Message>> createTask() {
        return new Task<>() {
            @Override
            protected List<Message> call() {
                messages.clear();
                EventQueues.toUI.drainTo(messages, 10);
                return messages;
            }
        };
    }
}
