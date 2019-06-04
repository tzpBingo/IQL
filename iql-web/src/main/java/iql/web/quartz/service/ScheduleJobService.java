package iql.web.quartz.service;

import iql.web.bean.ScheduleJob;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;

import java.util.List;

public interface ScheduleJobService {

    public List<ScheduleJob> getAllJobList(ScheduleJob scheduleJob);

    public List<ScheduleJob> getRunningJobList() throws SchedulerException;

    public void saveOrupdate(ScheduleJob scheduleJob) throws Exception;

    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException;

    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException;

    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException;

    public void runJobOnce(ScheduleJob scheduleJob) throws SchedulerException;

    public SchedulerMetaData getMetaData() throws SchedulerException;

}
