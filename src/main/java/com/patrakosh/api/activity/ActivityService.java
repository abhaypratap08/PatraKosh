package com.patrakosh.api.activity;

import com.patrakosh.persistence.AppStateStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class ActivityService {

    private final AppStateStore stateStore;

    public ActivityService(AppStateStore stateStore) {
        this.stateStore = stateStore;
    }

    public void record(long userId, String action, String filename) {
        stateStore.write(state -> {
            state.activities.add(new AppStateStore.ActivityRecord(
                    state.nextActivityId++,
                    userId,
                    action,
                    filename,
                    Instant.now()
            ));
            return null;
        });
    }

    public List<ActivityEntry> listForUser(long userId) {
        return stateStore.read(state -> state.activities.stream()
                .filter(entry -> entry.userId == userId)
                .sorted(Comparator.comparing((AppStateStore.ActivityRecord entry) -> entry.createdAt).reversed())
                .map(entry -> new ActivityEntry(entry.id, entry.action, entry.filename, entry.createdAt))
                .toList());
    }

    public record ActivityEntry(long id, String action, String filename, Instant createdAt) {
    }
}
