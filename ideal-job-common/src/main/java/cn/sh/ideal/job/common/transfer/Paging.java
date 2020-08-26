package cn.sh.ideal.job.common.transfer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 宋志宗
 * @date 2020/5/25
 */
public class Paging {
  /**
   * 页码
   */
  private int page = 1;
  /**
   * 页大小
   */
  private int size = 20;

  /**
   * 排序参数
   */
  @Nullable
  private List<Order> orders;

  @Nonnull
  public static Paging of(int page, int size) {
    return new Paging(page, size, null);
  }

  @Nonnull
  public static Paging of(int page, int size, @Nullable List<Order> orders) {
    return new Paging(page, size, orders);
  }

  @Nonnull
  public String toQueryString() {
    return "page=" + page + "&size=" + size;
  }

  /**
   * 添加升序排序条件
   *
   * @param property 排序字段
   */
  @Nonnull
  public Paging ascBy(@Nonnull String property) {
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders.add(Order.asc(property));
    return this;
  }

  /**
   * 添加降序排序条
   *
   * @param property 排序字段
   */
  @Nonnull
  public Paging descBy(@Nonnull String property) {
    if (orders == null) {
      orders = new ArrayList<>();
    }
    orders.add(Order.desc(property));
    return this;
  }

  public Paging() {
  }

  public Paging(int page, int size, @Nullable List<Order> orders) {
    this.page = page;
    this.size = size;
    this.orders = orders;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Nullable
  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(@Nullable List<Order> orders) {
    this.orders = orders;
  }

  public enum Direction {
    ASC, DESC;

    public boolean isAscending() {
      return this.equals(ASC);
    }

    public boolean isDescending() {
      return this.equals(DESC);
    }
  }

  public static class Order {
    /**
     * 排序字段名
     */
    private String property;
    /**
     * 排序方式
     */
    private Direction direction;

    public Order() {
    }

    public Order(String property, Direction direction) {
      this.direction = direction;
      this.property = property;
    }

    private static Order by(String property, Direction direction) {
      return new Order(property, direction);
    }

    public static Order asc(String property) {
      return new Order(property, Direction.ASC);
    }

    public static Order desc(String property) {
      return new Order(property, Direction.DESC);
    }

    public String getProperty() {
      return property;
    }

    public void setProperty(String property) {
      this.property = property;
    }

    public Direction getDirection() {
      return direction;
    }

    public void setDirection(Direction direction) {
      this.direction = direction;
    }
  }

}
