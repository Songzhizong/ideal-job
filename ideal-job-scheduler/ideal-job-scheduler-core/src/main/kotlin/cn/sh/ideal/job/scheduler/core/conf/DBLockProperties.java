package cn.sh.ideal.job.scheduler.core.conf;

/**
 * @author 宋志宗
 * @date 2020/8/29
 */
public class DBLockProperties {
  private String lockTable = "job_lock";
  private String lockColumn = "lock_name";
  private String scheduleLockName = "schedule_lock";

  public String getLockTable() {
    return lockTable;
  }

  public void setLockTable(String lockTable) {
    this.lockTable = lockTable;
  }

  public String getLockColumn() {
    return lockColumn;
  }

  public void setLockColumn(String lockColumn) {
    this.lockColumn = lockColumn;
  }

  public String getScheduleLockName() {
    return scheduleLockName;
  }

  public void setScheduleLockName(String scheduleLockName) {
    this.scheduleLockName = scheduleLockName;
  }
}
