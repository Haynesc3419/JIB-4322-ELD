package com.michelin.connectedfleet.ELD_Backend;

import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.CreateLogEntryRequest;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.GetLogEntryResponseItem;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntry;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        LogEntry entry = new LogEntry((String)(session.getAttribute("username")), entryRequest.status());
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

        return new ResponseEntity<>(entries, HttpStatus.OK);
    }
}
