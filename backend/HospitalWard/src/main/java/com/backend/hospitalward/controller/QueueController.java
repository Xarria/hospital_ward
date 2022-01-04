package com.backend.hospitalward.controller;

import com.backend.hospitalward.dto.response.queue.QueueResponse;
import com.backend.hospitalward.mapper.QueueMapper;
import com.backend.hospitalward.security.annotation.Authenticated;
import com.backend.hospitalward.security.annotation.MedicAuthorities;
import com.backend.hospitalward.service.QueueService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping
public class QueueController {

    QueueMapper queueMapper;

    QueueService queueService;

    @MedicAuthorities
    @GetMapping("/queues")
    public ResponseEntity<List<QueueResponse>> getAllCurrentQueues() {
        return ResponseEntity.ok(queueService.getAllCurrentQueues().stream()
                .map(queueMapper::toQueueResponse)
                .collect(Collectors.toList()));
    }

    @MedicAuthorities
    @GetMapping("/queues/{queueDate}")
    public ResponseEntity<QueueResponse> getQueueByDate(@PathVariable("queueDate")
                                                        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate queueDate) {
        return ResponseEntity.ok(queueMapper.toQueueResponse(queueService.getQueueForDate(queueDate)));
    }

    @Authenticated
    @GetMapping("/fulldates")
    public ResponseEntity<List<LocalDate>> getFullAdmissionDates() {
        return ResponseEntity.ok(queueService.findFullAdmissionDates());
    }

}
