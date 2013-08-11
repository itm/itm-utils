package de.uniluebeck.itm.util.scheduler;

import com.google.common.util.concurrent.Service;

import java.util.concurrent.ScheduledExecutorService;

public interface SchedulerService extends Service, ScheduledExecutorService {

}
