package com.prox.challenge.gcoder.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.TimeZone;

@Service
public class TimeService {
    public static void setTimeGM7(){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+7:00"));
    }
    public static void timer(int hour, int minute, int second, int dayStep) throws InterruptedException {
        long waitTime;
        // lay thoi gian tuy chinh trong ngay
        LocalDateTime timeCustom = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(second);
        // neu thoi gian tuy chinh nho hon thoi gian hien tai thi lay thoi diem ngay hom sau
        if (System.currentTimeMillis() > ConvertToMillisecond(timeCustom)) {
            timeCustom = timeCustom.plusDays(dayStep);
        }
        // lay thoi gian cho giua 2 thoi diem
        waitTime = ConvertToMillisecond(timeCustom) - System.currentTimeMillis();
        Thread.sleep(waitTime);
    }

    public static long ConvertToMillisecond(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(OffsetDateTime.now().getOffset());
        return instant.toEpochMilli();
    }
    /**
     * calculate the time from the current time to the target time of day, the return value is milli Seconds
     * tính thời gian từ thời điểm hiện tại cho đến thời gian mục tiêu trong ngày, giá trị trả về là milliSeconds
     */
    public long calculatorMilliSecondsToTimeOfDay(int hour, int minute, int second){
        // Lấy thời điểm hiện tại
        Calendar now = Calendar.getInstance();

        // Đặt thời gian khởi động vào 10:00 AM (thay đổi theo nhu cầu)
        Calendar scheduledTime = Calendar.getInstance();
        scheduledTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduledTime.set(Calendar.MINUTE, minute);
        scheduledTime.set(Calendar.SECOND, second);

        // Tính thời gian trễ đến thời điểm khởi động
        long initialDelay = scheduledTime.getTimeInMillis() - now.getTimeInMillis();
        if (initialDelay < 0) {
            // Nếu thời gian đã trôi qua thì thêm 1 ngày (hoặc tùy ý) để đặt lịch cho ngày mai
            initialDelay += 24 * 60 * 60 * 1000; // 24 giờ
        }
        return initialDelay;
    }
}
