package com.backend.hospitalward.schedule;

import com.backend.hospitalward.model.Patient;
import com.backend.hospitalward.service.QueueService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class QueueScheduler {

    final QueueService queueService;

    @Scheduled(cron = "0 15 0 * * *")
    public void transferWaitingPatientsFromYesterdayQueue() {
        List<Patient> waitingPatients = queueService.getWaitingPatientsForPastQueues();

        if (waitingPatients == null || waitingPatients.isEmpty()) {
            return;
        }

        queueService.transferPatientsForNextUnlockedDateAndClearOldQueues(LocalDate.now(), waitingPatients);

    }
}
