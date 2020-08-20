package cn.sh.ideal.nj.cmpt.job.scheduler.core.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

/**
 * 用户
 *
 * @author 宋志宗
 * @date 2020/8/20
 */
@Entity
@Table(
    name = "job_user",
    indexes = {
        @Index(name = "username", columnList = "username"),
    }
)
@org.hibernate.annotations.Table(appliesTo = "job_user", comment = "用户")
@SQLDelete(sql = "update job_user set deleted = 1 where user_id = ?")
@Where(clause = "deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class JobUser {
  /**
   * 用户Id
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "job_user_generator")
  @GenericGenerator(name = "job_user_generator",
      strategy = "cn.sh.ideal.nj.cmpt.job.scheduler.core.generator.JpaIdentityGenerator",
      parameters = {@org.hibernate.annotations.Parameter(name = "biz", value = "job_user")})
  @Column(name = "user_id", nullable = false, updatable = false
      , columnDefinition = "bigint(20) comment '用户Id'"
  )
  private Long userId;

  /**
   * 账号
   */
  @Column(
      name = "account", nullable = false, length = 64
      , columnDefinition = "varchar(64) comment '账号'"
  )
  private String account;

  /**
   * 账号
   */
  @Column(
      name = "password", nullable = false, length = 64
      , columnDefinition = "varchar(64) comment 'password'"
  )
  private String password;

  /**
   * 角色：0-普通用户、1-管理员
   */
  @Column(
      name = "role", nullable = false
      , columnDefinition = "int(11) comment '角色：0-普通用户、1-管理员'"
  )
  private int role;

  /**
   * 权限：执行器ID列表，多个逗号分割
   */
  @Column(
      name = "permission", nullable = false, length = 64
      , columnDefinition = "varchar(64) comment '权限：执行器ID列表，多个逗号分割'"
  )
  private String permission;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getRole() {
    return role;
  }

  public void setRole(int role) {
    this.role = role;
  }

  public String getPermission() {
    return permission;
  }

  public void setPermission(String permission) {
    this.permission = permission;
  }
}
