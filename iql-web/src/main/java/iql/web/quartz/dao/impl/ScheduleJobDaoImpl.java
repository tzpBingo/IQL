package iql.web.quartz.dao.impl;

import iql.web.bean.ScheduleJob;
import iql.web.quartz.dao.ScheduleJobDao;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class ScheduleJobDaoImpl implements ScheduleJobDao {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	public List<ScheduleJob> findScheduleJobScriptList(ScheduleJob job) {
		String con = "";
		if(job.getJobStatus()!=null && job.getJobStatus().length()>0){
			con+=" AND tri.TRIGGER_STATE='"+job.getJobStatus()+"'";
		}
		if(job.getJobName()!=null && job.getJobName().length()>0){
			con+=" AND job.JOB_NAME like '%"+job.getJobName()+"%'";
		}
		String sql = "SELECT " +
				"detail.id AS jobId, " +
				"concat( job.JOB_GROUP, '.', job.JOB_NAME ) as jobkey, " +
				"job.JOB_NAME AS jobName, " +
				"job.JOB_GROUP AS jobGroup, " +
				"job.DESCRIPTION AS description, " +
				"job.JOB_CLASS_NAME AS interfaceName, " +
				"cron.CRON_EXPRESSION AS cronExpression, " +
				"tri.TRIGGER_STATE AS jobStatus, " +
				"detail.email, " +
				"detail.multiple_executions multipleExecutions, " +
				"detail.execute_times executeTimes, " +
				"detail.retry, " +
				"detail.retry_interval retryInterval, " +
				"detail.script, " +
				"detail.start_time startTime, " +
				"detail.end_time endTime " +
				"FROM " +
				"QRTZ_JOB_DETAILS AS job " +
				"LEFT JOIN QRTZ_TRIGGERS AS tri ON job.JOB_NAME = tri.JOB_NAME " +
				"LEFT JOIN QRTZ_CRON_TRIGGERS AS cron ON cron.TRIGGER_NAME = tri.TRIGGER_NAME " +
				"LEFT JOIN schedule_job_script AS detail ON detail.jobkey = concat( job.JOB_GROUP, '.', job.JOB_NAME )  " +
				"WHERE " +
				"tri.TRIGGER_TYPE = 'CRON' " +con;

		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.getResultList();
	}

	@Override
	public Integer findCount() {
		String sql = "select FOUND_ROWS()";
		Query query = em.createNativeQuery(sql);
		Integer total = ((Number)query.getSingleResult()).intValue();
		return total;
	}

	@Override
	public Integer addScheduleJobScript(ScheduleJob job) {
		String sql = "insert into schedule_job_script(jobkey, script, email, execute_times, multiple_executions, retry, retry_interval, description, start_time, end_time) " +
				"values(:jobkey,:script,:email,:execute_times,:multiple_executions,:retry,:retry_interval,:description,:start_time,:end_time) ";
		Query query = em.createNativeQuery(sql);
		query.setParameter("jobkey", job.getJobGroup()+"."+job.getJobName());
		query.setParameter("script", job.getScript());
		query.setParameter("email", job.getEmail());
		query.setParameter("execute_times", job.getExecuteTimes());
		query.setParameter("multiple_executions", job.getMultipleExecutions());
		query.setParameter("retry", job.getRetry());
		query.setParameter("retry_interval", job.getRetryInterval());
		query.setParameter("description", job.getDescription());
		query.setParameter("start_time", job.getStartTime());
		query.setParameter("end_time", job.getEndTime());
		return query.executeUpdate();
	}

	@Override
	public Integer updateScheduleJobScript(ScheduleJob job) {
		String sql="UPDATE schedule_job_script " +
				"SET script =:script," +
				"email =:email," +
				"execute_times =:execute_times," +
				"multiple_executions =:multiple_executions," +
				"retry =:retry," +
				"retry_interval =:retry_interval," +
				"description =:description," +
				"start_time =:start_time," +
				"end_time =:end_time " +
				"WHERE " +
				"id = :id ";
		Query query = em.createNativeQuery(sql);
		query.setParameter("id", job.getJobId());
		query.setParameter("script", job.getScript());
		query.setParameter("email", job.getEmail());
		query.setParameter("execute_times", job.getExecuteTimes());
		query.setParameter("retry", job.getRetry());
		query.setParameter("retry_interval", job.getRetryInterval());
		query.setParameter("multiple_executions", job.getMultipleExecutions());
		query.setParameter("description", job.getDescription());
		query.setParameter("start_time", job.getStartTime());
		query.setParameter("end_time", job.getEndTime());
		return query.executeUpdate();
	}

	@Override
	public Integer delScheduleJobScript(ScheduleJob job) {
		String sql = "delete from schedule_job_script where id=:id";
		Query query = em.createNativeQuery(sql);
		query.setParameter("id", job.getJobId());
		return query.executeUpdate();
	}

	@Override
	public ScheduleJob findScheduleJobScriptByJobKey(String jobKey) {
		String sql = "SELECT " +
				"detail.id AS jobId, " +
				"concat( job.JOB_GROUP, '.', job.JOB_NAME ) as jobkey, " +
				"job.JOB_NAME AS jobName, " +
				"job.JOB_GROUP AS jobGroup, " +
				"job.DESCRIPTION AS description, " +
				"job.JOB_CLASS_NAME AS interfaceName, " +
				"cron.CRON_EXPRESSION AS cronExpression, " +
				"tri.TRIGGER_STATE AS jobStatus, " +
				"detail.email, " +
				"detail.multiple_executions multipleExecutions, " +
				"detail.execute_times executeTimes, " +
				"detail.retry, " +
				"detail.retry_interval retryInterval, " +
				"detail.script, " +
				"detail.start_time startTime, " +
				"detail.end_time endTime  " +
				"FROM " +
				"QRTZ_JOB_DETAILS AS job " +
				"LEFT JOIN QRTZ_TRIGGERS AS tri ON job.JOB_NAME = tri.JOB_NAME " +
				"LEFT JOIN QRTZ_CRON_TRIGGERS AS cron ON cron.TRIGGER_NAME = tri.TRIGGER_NAME " +
				"LEFT JOIN schedule_job_script AS detail ON detail.jobkey = concat( job.JOB_GROUP, '.', job.JOB_NAME )  " +
				"WHERE tri.TRIGGER_TYPE = 'CRON' AND " +
				"detail.jobkey = :jobkey ";

		Query query = em.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.aliasToBean(ScheduleJob.class));
		query.setParameter("jobkey",jobKey);
		List<ScheduleJob> res = query.getResultList();
		return res.size()==1 ? res.get(0) : null;
	}
}
