package iql.web.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
public class ScheduleJob implements Serializable{

	private static final long serialVersionUID = 1L;

	@Column(name = "id")
    private Integer jobId;
    
    private String jobName;
    
    private String jobGroup;
    
    private String jobStatus;
    
    private String cronExpression;

    private String description;
    
    private String interfaceName;

	private String jobkey;

	private String email;

	@Column(name = "execute_times")
	private Integer executeTimes;

	@Column(name = "multiple_executions")
	private String multipleExecutions;

	private Integer retry;

	@Column(name = "retry_interval")
	private Integer retryInterval;

	@Column(name = "start_time")
	private String startTime;

	@Column(name = "end_time")
	private String endTime;

	private String script;

	public ScheduleJob(){

	}


	@Override
	public String toString() {
		return "ScheduleJob [jobId=" + jobId + ", jobName=" + jobName + ", jobGroup=" + jobGroup + ", jobStatus="
				+ jobStatus + ", cronExpression=" + cronExpression + ", desc=" + description + ", interfaceName="
				+ interfaceName + "]";
	}

}
