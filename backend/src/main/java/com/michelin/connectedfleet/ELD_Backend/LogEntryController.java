package com.michelin.connectedfleet.ELD_Backend;

import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.CreateLogEntryRequest;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.GetLogEntryResponseItem;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntry;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntryRepository;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.Event;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("logs")
public class LogEntryController {
    private final LogEntryRepository logEntryRepository;

    public LogEntryController(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @PostMapping("/insertEntry")
    public ResponseEntity<?> insertEntry(@Validated @RequestBody CreateLogEntryRequest entryRequest, HttpSession session) {
        if (session.isNew() && entryRequest.token() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = (session.isNew()) ? entryRequest.token() : (String)(session.getAttribute("username"));
        LogEntry entry = new LogEntry((String)(session.getAttribute("username")), entryRequest.status(), "0");
        logEntryRepository.insert(entry);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getEntries(HttpSession session) {
        if (session.isNew()) {
            session.invalidate();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String username = (String)(session.getAttribute("username"));

        List<LogEntry> entries = logEntryRepository.findAllByUsername(username);
        List<GetLogEntryResponseItem> transformedEntries = entries.stream()
                .map(GetLogEntryResponseItem::new)
                .toList();

        System.out.println(entries.get(0).getVerifiedByDriver());
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @PutMapping("/updateEntry")
    public ResponseEntity<?> updateEntry(@RequestBody LogEntry logEntry, HttpSession session) {
        System.out.println("Updating entry: " + logEntry.toString());


        String id = logEntry.getId();
        Optional<LogEntry> existingEntry = logEntryRepository.findById(id);
        if (existingEntry.isEmpty()) {
            return new ResponseEntity<>("Log entry not found", HttpStatus.NOT_FOUND);
        }

        logEntryRepository.save(logEntry); // Save updates

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verifyEntry(@RequestBody String id) {
        // strip leading and ending quotes
        id = id.substring(1, id.length() - 1);
        Optional<LogEntry> logEntry = logEntryRepository.findById(id);
        if (logEntry.isPresent()) {
            LogEntry entry = logEntry.get();
            entry.setVerifiedByDriver("1");
            logEntryRepository.save(entry);

            System.out.println("Verify success");
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            System.out.println("Verify failed");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
