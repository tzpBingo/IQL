package iql.web.quartz.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import iql.web.bean.Message;
import iql.web.bean.ScheduleJob;
import iql.web.quartz.service.ScheduleJobService;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/job")
public class ScheduleJobAction {

	private static Logger logger = LoggerFactory.getLogger(ScheduleJobAction.class);

	@Autowired
	private ScheduleJobService scheduleJobService;
	
	@RequestMapping("/meta")
	public Object metaData() throws SchedulerException{
		SchedulerMetaData metaData = scheduleJobService.getMetaData();
		return metaData;
	}
	
	@RequestMapping("/all")
	public Object getAllJobs(ScheduleJob job){
		List<ScheduleJob> jobList = scheduleJobService.getAllJobList(job);
		JSONObject res = new JSONObject();
		int total = jobList.size();
		res.put("total", total);
		res.put("rows", JSON.parseArray(JSON.toJSONString(jobList)));
		return res;
	}
	
	@RequestMapping("/running")
	public Object getRunningJobs() throws SchedulerException{
		List<ScheduleJob> jobList = scheduleJobService.getRunningJobList();
		return jobList;
	}
	
	@RequestMapping(value = "/pause", method = {RequestMethod.GET, RequestMethod.POST})
	public Object pauseJob(ScheduleJob job){
		logger.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.pauseJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			logger.error("pauseJob ex:", e);
		}
		return message;
	}
	
	@RequestMapping(value = "/resume", method = {RequestMethod.GET, RequestMethod.POST})
	public Object resumeJob(ScheduleJob job){
		logger.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.resumeJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			logger.error("resumeJob ex:", e);
		}
		return message;
	}
	
	
	@RequestMapping(value = "/delete", method = {RequestMethod.GET, RequestMethod.POST})
	public Object deleteJob(ScheduleJob job){
		logger.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.deleteJob(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			logger.error("deleteJob ex:", e);
		}
		return message;
	}
	
	@RequestMapping(value = "/run", method = {RequestMethod.GET, RequestMethod.POST})
	public Object runJob(ScheduleJob job){
		logger.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.runJobOnce(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			logger.error("runJob ex:", e);
		}
		return message;
	}
	
	
	@RequestMapping(value = "/add", method = {RequestMethod.GET, RequestMethod.POST})
	public Object saveOrupdate(ScheduleJob job){
		logger.info("params, job = {}", job);
		Message message = Message.failure();
		try {
			scheduleJobService.saveOrupdate(job);
			message = Message.success();
		} catch (Exception e) {
			message.setMsg(e.getMessage());
			logger.error("updateCron ex:", e);
		}
		return message;
	}
	

}
