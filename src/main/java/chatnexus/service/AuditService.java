package chatnexus.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class AuditService {
    public static class Event {
        private String type;
        private String action;
        private String actor;
        private String target;
        private String details;
        private long timestamp;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getActor() { return actor; }
        public void setActor(String actor) { this.actor = actor; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<>();

    public void record(String type, String action, String actor, String target, String details) {
        Event e = new Event();
        e.setType(type);
        e.setAction(action);
        e.setActor(actor);
        e.setTarget(target);
        e.setDetails(details);
        e.setTimestamp(Instant.now().toEpochMilli());
        events.add(e);
    }

    public List<Event> list(String type) {
        return events.stream()
                .filter(e -> type == null || type.equalsIgnoreCase(e.getType()))
                .collect(Collectors.toList());
    }
}