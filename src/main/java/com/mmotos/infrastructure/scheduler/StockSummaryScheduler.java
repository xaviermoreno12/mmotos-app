package com.mmotos.infrastructure.scheduler;

import com.mmotos.domain.port.NotificationPort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockSummaryScheduler {

    private final NotificationPort notificationPort;

    public StockSummaryScheduler(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    // Todos los días a las 20:00 (cierre)
    @Scheduled(cron = "0 0 20 * * *")
    public void preguntarResumenStock() {
        notificationPort.preguntarResumenStock();
    }
}
