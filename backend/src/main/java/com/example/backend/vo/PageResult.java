package com.example.backend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果对象。
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

	/**
	 * 当前页数据。
	 */
	private List<T> records;

	/**
	 * 总记录数。
	 */
	private Long total;

	/**
	 * 当前页码。
	 */
	private Long current;

	/**
	 * 每页条数。
	 */
	private Long size;

	/**
	 * 总页数。
	 */
	private Long pages;
}
