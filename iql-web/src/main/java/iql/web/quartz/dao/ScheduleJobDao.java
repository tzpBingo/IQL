package iql.web.quartz.dao;

import iql.web.bean.ScheduleJob;

import java.util.List;


public interface ScheduleJobDao {

	public List<ScheduleJob> findScheduleJobScriptList(ScheduleJob job);

	public Integer findCount();

	public Integer addScheduleJobScript(ScheduleJob job);

	public Integer updateScheduleJobScript(ScheduleJob job);

	public Integer delScheduleJobScript(ScheduleJob job);

	public ScheduleJob findScheduleJobScriptByJobKey(String jobKey);


}
