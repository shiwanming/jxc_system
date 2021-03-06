package com.mf.service;

import java.util.List;

import com.mf.entity.CustomerReturnListGoods;

/**
 * 客户退货单材料Service接口
 * @author Administrator
 *
 */
public interface CustomerReturnListGoodsService {

	/**
	 * 根据客户退货单id查询所有客户退货单材料
	 * @param customerReturnListId
	 * @return
	 */
	public List<CustomerReturnListGoods> listByCustomerReturnListId(Integer customerReturnListId);

	/**
	 * 统计某个材料的退货总数
	 * @param goodsId
	 * @return
	 */
	public Integer getTotalByGoodsId(Integer goodsId);

	/**
	 * 根据条件查询客户退货单材料
	 * @param customerReturnListGoods
	 * @return
	 */
	public List<CustomerReturnListGoods> list(CustomerReturnListGoods customerReturnListGoods);

}
