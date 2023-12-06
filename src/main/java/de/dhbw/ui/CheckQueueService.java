package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.UIMessage;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class CheckQueueService extends ScheduledService<List<UIMessage>> {
    private  final List<UIMessage> messages = new ArrayList<>(20);

    @Override
    protected Task<List<UIMessage>> createTask() {
        return new Task<>() {
            @Override
            protected List<UIMessage> call() {
                messages.clear();
                EventQueues.toUI.drainTo(messages, 20);
                return messages;
            }
        };
    }
}
