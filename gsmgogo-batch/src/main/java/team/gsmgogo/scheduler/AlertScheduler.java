package team.gsmgogo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import team.gsmgogo.domain.match.entity.MatchEntity;
import team.gsmgogo.domain.match.repository.MatchQueryDslRepository;
import team.gsmgogo.job.AlertJob;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final Scheduler scheduler;
    private final MatchQueryDslRepository matchQueryDslRepository;

    @Scheduled(cron = "0 27 18 * * *")
    public void start(){
        LocalDate today = LocalDate.now();
        List<MatchEntity> matches = matchQueryDslRepository.findByMonthAndDay(
            today.getMonthValue(),
            today.getDayOfMonth()
        );

        matches.forEach(match -> {
            try {
                LocalDateTime beforeMatch = match.getStartAt().minusMinutes(10);

                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("matchId", match.getMatchId());

                JobDetailImpl detail1 = new JobDetailImpl();
                detail1.setName("alert-detail-" + UUID.randomUUID());
                detail1.setGroup("alert");
                detail1.setJobClass(AlertJob.class);
                detail1.setJobDataMap(jobDataMap);

                Trigger trigger1 = TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 %d %d %d %d ? %d".formatted(
                        beforeMatch.getMinute(),
                        beforeMatch.getHour(),
                        beforeMatch.getDayOfMonth(),
                        beforeMatch.getMonthValue(),
                        beforeMatch.getYear()
                    ))).build();

                scheduler.scheduleJob(detail1, trigger1);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }
}