package iql.web.quartz.service.impl;

import com.google.common.base.Preconditions;
import iql.web.bean.ScheduleJob;
import iql.web.quartz.dao.ScheduleJobDao;
import iql.web.quartz.job.QuartzJobFactory;
import iql.web.quartz.service.ScheduleJobService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Service
public class ScheduleJobServiceImpl implements ScheduleJobService {

	@Autowired
	private Scheduler scheduler;

    @Autowired
    private ScheduleJobDao scheduleJobDao;

    public List<ScheduleJob> getAllJobList(ScheduleJob job){
        List<ScheduleJob> jobList = scheduleJobDao.findScheduleJobScriptList(job);
        return jobList;  
    } 
	
	
    public List<ScheduleJob> getRunningJobList() throws SchedulerException{
        List<JobExecutionContext> executingJobList = scheduler.getCurrentlyExecutingJobs();
        List<ScheduleJob> jobList = new ArrayList<>(executingJobList.size());
        for(JobExecutionContext executingJob : executingJobList){  
            ScheduleJob scheduleJob = new ScheduleJob();
            JobDetail jobDetail = executingJob.getJobDetail();  
            JobKey jobKey = jobDetail.getKey();  
            Trigger trigger = executingJob.getTrigger();  
            this.wrapScheduleJob(scheduleJob,scheduler,jobKey,trigger);  
            jobList.add(scheduleJob);  
        }  
        return jobList;  
    }

    @Transactional
    public void saveOrupdate(ScheduleJob scheduleJob) throws Exception {
        checkNotNull(scheduleJob);

		if (!scheduler.checkExists(new JobKey(scheduleJob.getJobName(),scheduleJob.getJobGroup()))) {
			addJob(scheduleJob);
            scheduleJobDao.addScheduleJobScript(scheduleJob);
		}else {
			updateJobCronExpression(scheduleJob);
            scheduleJobDao.updateScheduleJobScript(scheduleJob);
		}
	}

    private void addJob(ScheduleJob scheduleJob) throws Exception{
    	checkNotNull(scheduleJob);

		Preconditions.checkNotNull(StringUtils.isEmpty(scheduleJob.getCronExpression()), "CronExpression is null");
		
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());  
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if(trigger != null){
            throw new Exception("job already exists!");
        }
        JobDetail jobDetail = JobBuilder
                .newJob(QuartzJobFactory.class)
                .withIdentity(scheduleJob.getJobName(),scheduleJob.getJobGroup())
                .withDescription(scheduleJob.getDescription())
                .build();
        jobDetail.getJobDataMap().put("scheduleJob", scheduleJob);
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
//        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(scheduleJob.getRetryInterval()).withRepeatCount(scheduleJob.getRetry());
        trigger = TriggerBuilder.newTrigger()
                .withIdentity(scheduleJob.getJobName(), scheduleJob.getJobGroup())
                .withSchedule(cronScheduleBuilder)
                .build();
        scheduler.scheduleJob(jobDetail, trigger);

        if("PAUSED".equals(scheduleJob.getJobStatus())){
            scheduler.pauseTrigger(triggerKey);
        }
  
    }

    @Transactional
    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException{
    	checkNotNull(scheduleJob);
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
        scheduleJobDao.updateScheduleJobScript(scheduleJobDao.findScheduleJobScriptByJobKey(jobKey.toString()));
    }

    @Transactional
    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException{
    	checkNotNull(scheduleJob);
    	JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
        scheduleJobDao.updateScheduleJobScript(scheduleJobDao.findScheduleJobScriptByJobKey(jobKey.toString()));
    }

    @Transactional
    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException{
    	checkNotNull(scheduleJob);
        Preconditions.checkNotNull(StringUtils.isEmpty(scheduleJob.getJobId()), "jobId is null");
        scheduleJobDao.delScheduleJobScript(scheduleJob);
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());  
        scheduler.deleteJob(jobKey);  
    }
    
    public void runJobOnce(ScheduleJob scheduleJob) throws SchedulerException{
    	checkNotNull(scheduleJob);
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());  
        scheduler.triggerJob(jobKey);  
    } 
    
    
    private void updateJobCronExpression(ScheduleJob scheduleJob) throws SchedulerException{
		checkNotNull(scheduleJob);

		Preconditions.checkNotNull(StringUtils.isEmpty(scheduleJob.getCronExpression()), "CronExpression is null");

        JobDetail jobDetail = JobBuilder
                .newJob(QuartzJobFactory.class)
                .withIdentity(scheduleJob.getJobName(),scheduleJob.getJobGroup())
                .withDescription(scheduleJob.getDescription())
                .storeDurably(true)
                .build();
        jobDetail.getJobDataMap().put("scheduleJob", scheduleJob);

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        CronTrigger cronTrigger = (CronTrigger)scheduler.getTrigger(triggerKey);
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());
        cronTrigger = cronTrigger.getTriggerBuilder()
                .withIdentity(triggerKey)
                .withSchedule(cronScheduleBuilder)
                .forJob(jobDetail)
                .build();

        scheduler.addJob(jobDetail,true);
        scheduler.rescheduleJob(triggerKey, cronTrigger);

        if("PAUSED".equals(scheduleJob.getJobStatus())){
            scheduler.pauseTrigger(triggerKey);
        }
    }
    
    private void wrapScheduleJob(ScheduleJob scheduleJob, Scheduler scheduler, JobKey jobKey, Trigger trigger){
        try {  
            scheduleJob.setJobName(jobKey.getName());  
            scheduleJob.setJobGroup(jobKey.getGroup()); 
  
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);  
            ScheduleJob job = (ScheduleJob)jobDetail.getJobDataMap().get("scheduleJob");
            scheduleJob.setDescription(jobDetail.getDescription());
            scheduleJob.setJobId(job.getJobId());
  
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());  
            scheduleJob.setJobStatus(triggerState.name());  
            if(trigger instanceof CronTrigger){  
                CronTrigger cronTrigger = (CronTrigger)trigger;  
                String cronExpression = cronTrigger.getCronExpression();  
                scheduleJob.setCronExpression(cronExpression);  
            }  
        } catch (SchedulerException e) {  
            e.printStackTrace(); 
        }  
    }

    private void checkNotNull(ScheduleJob scheduleJob) {
    	Preconditions.checkNotNull(scheduleJob, "job is null");
		Preconditions.checkNotNull(StringUtils.isEmpty(scheduleJob.getJobName()), "jobName is null");
		Preconditions.checkNotNull(StringUtils.isEmpty(scheduleJob.getJobGroup()), "jobGroup is null");
	}
    
    
	public SchedulerMetaData getMetaData() throws SchedulerException {
		SchedulerMetaData metaData = scheduler.getMetaData();
		return metaData;
	}

	
}
