package com.zzsong.job.common.transfer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 宋志宗
 * @date 2020/5/15
 */
public final class SpringPages {

  /**
   * 将{@link Page}转换为带分页相关数据的{@link Res}对象
   *
   * @param page {@link Page}
   * @param <T>  page中携带的content类型
   * @return {@link Res} data中的数据为{@link Page#getContent()}
   */
  @Nonnull
  public static <T> Res<List<T>> toPageRes(@Nonnull Page<T> page) {
    List<T> content = page.getContent();
    return toPageRes(page, content);
  }

  /**
   * 将{@link Page}转换为带分页相关数据的{@link Res}对象
   *
   * @param page     {@link Page}
   * @param function 对{@link Page}中的content进行类型转换
   * @param <S>      {@link Page} 中的原始类型
   * @param <R>      function的返回类型
   * @return {@link Res} datas
   */
  @Nonnull
  public static <S, R> Res<List<R>> toPageRes(@Nonnull Page<S> page,
                                              @Nonnull Function<S, R> function) {
    List<S> content = page.getContent();
    List<R> data = new ArrayList<>();
    for (S s : content) {
      data.add(function.apply(s));
    }
    return toPageRes(page, data);
  }

  /**
   * 将{@link Page}转换为带分页相关数据的{@link Res}对象
   *
   * @param page     {@link Page}
   * @param dataList 响应数据
   * @param <T>      datas的泛型
   * @return {@link Res} dataList
   */
  @Nonnull
  public static <T> Res<List<T>> toPageRes(@Nonnull Page<?> page, @Nonnull List<T> dataList) {
    long totalElements = page.getTotalElements();
    int totalPages = page.getTotalPages();
    int number = page.getNumber();
    int size = page.getSize();
    return Res.<List<T>>builder()
        .success(true)
        .code(CommonResMsg.SUCCESS.code())
        .message(CommonResMsg.SUCCESS.message())
        .data(dataList)
        // spring的页码从0开始, 我们的从1开始
        .page(number + 1)
        .size(size)
        .total(totalElements)
        .totalPages(totalPages)
        .build();
  }

  /**
   * 将{@link Paging}转换为Spring的{@link Pageable}
   *
   * @param paging {@link Paging}
   * @return {@link Pageable}
   */
  @Nonnull
  public static Pageable paging2Pageable(@Nonnull Paging paging) {
    int page = paging.getPage();
    int size = paging.getSize();
    List<Paging.Order> orders = paging.getOrders();

    List<Sort.Order> orderList = null;
    if (orders != null && orders.size() > 0) {
      orderList = new ArrayList<>();
      for (Paging.Order order : orders) {
        Paging.Direction direction = order.getDirection();
        if (direction.isAscending()) {
          orderList.add(Sort.Order.asc(order.getProperty()));
        } else {
          orderList.add(Sort.Order.desc(order.getProperty()));
        }
      }
    }
    // 我们的页码从1开始, spring的页码从0开始, 转换一下
    if (orderList != null) {
      return PageRequest.of(page - 1, size, Sort.by(orderList));
    } else {
      return PageRequest.of(page - 1, size);
    }
  }

  /**
   * 将Spring的 {@link Pageable} 转换为 {@link Paging}
   *
   * @param pageable {@link Pageable}
   * @return {@link Paging}
   */
  @Nonnull
  public static Paging pageable2Paging(@Nonnull Pageable pageable) {
    List<Paging.Order> orders = pageable.getSort().get().map(order -> {
      String property = order.getProperty();
      if (order.getDirection().isAscending()) {
        return Paging.Order.asc(property);
      } else {
        return Paging.Order.desc(property);
      }
    }).collect(Collectors.toList());
    // spring的页码从0开始, 我们的从1开始
    if (orders.size() > 0) {
      return Paging.of(pageable.getPageNumber() + 1, pageable.getPageSize(), orders);
    } else {
      return Paging.of(pageable.getPageNumber() + 1, pageable.getPageSize());
    }
  }
}
