package com.michelin.connectedfleet.ELD_Backend;

import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.CreateLogEntryRequest;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntry;
import com.michelin.connectedfleet.ELD_Backend.data.LogEntry.LogEntryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
