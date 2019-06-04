package iql.web.quartz.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import iql.web.bean.ScheduleJob;
import iql.web.quartz.dao.ScheduleJobDao;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class QuartzJobFactory implements Job{

	private static Logger logger = LoggerFactory.getLogger(QuartzJobFactory.class);

	@Autowired
	private ScheduleJobDao scheduleJobDao;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
		JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
		ScheduleJob job = scheduleJobDao.findScheduleJobScriptByJobKey(jobKey.toString());
		Scheduler scheduler = jobExecutionContext.getScheduler();
		int count = jobExecutionContext.getRefireCount();
		int retry = job.getRetry();
		int retryInterval = job.getRetryInterval();
		String script = job.getScript();
		String exceStatus = count>0 ? "retry "+count : "running";
		if(count > retry && retry!=0){
			logger.error("retry {} times job {} over.", count-1, jobKey);
			JobExecutionException e = new JobExecutionException("Retries job "+jobKey+" exceeded");
			throw e;
		}
		try{
			logger.info("{} job {}.", exceStatus, jobKey);
			String res = Request.Post("http://localhost:8888/queryApi")
					.bodyForm(Form.form().add("iql", script).add("token","fa39e32c09332d47f6f38d9c946cfa25").build())
					.execute().returnContent().asString();

			JSONObject jsonObject = JSON.parseObject(res);
			if (!jsonObject.getBoolean("isSuccess")) {
				throw new Exception(jsonObject.getString("data"));
			}
		}catch(Exception e){
			if(retry==0){
				e.printStackTrace();
			}else{
				if(count <= retry){
					try {
						Thread.sleep(retryInterval*1000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					JobExecutionException jee = new JobExecutionException(e);
					jee.setRefireImmediately(true);
					jee.refireImmediately();
					throw jee;
				}
			}
		}

	}

}
