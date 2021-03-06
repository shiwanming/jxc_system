package com.mf.service.impl;


import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.mf.entity.Goods;
import com.mf.entity.CustomerReturnList;
import com.mf.entity.CustomerReturnListGoods;
import com.mf.repository.GoodsRepository;
import com.mf.repository.GoodsTypeRepository;
import com.mf.repository.CustomerReturnListGoodsRepository;
import com.mf.repository.CustomerReturnListRepository;
import com.mf.service.CustomerReturnListService;
import com.mf.util.StringUtil;

/**
 * 客户退货单Service实现类
 * @author Administrator
 *
 */
@Service("customerReturnListService")
public class CustomerReturnListServiceImpl implements CustomerReturnListService{

	@Resource
	private CustomerReturnListRepository customerReturnListRepository;

	@Resource
	private GoodsTypeRepository goodsTypeRepository;

	@Resource
	private GoodsRepository goodsRepository;

	@Resource
	private CustomerReturnListGoodsRepository customerReturnListGoodsRepository;

	@Override
	public String getTodayMaxCustomerReturnNumber() {
		return customerReturnListRepository.getTodayMaxCustomerReturnNumber();
	}

	@Transactional
	public void save(CustomerReturnList customerReturnList, List<CustomerReturnListGoods> customerReturnListGoodsList) {
		for(CustomerReturnListGoods customerReturnListGoods:customerReturnListGoodsList){
			customerReturnListGoods.setType(goodsTypeRepository.findOne(customerReturnListGoods.getTypeId())); // 设置类别
			customerReturnListGoods.setCustomerReturnList(customerReturnList); // 设置客户退货单
			customerReturnListGoodsRepository.save(customerReturnListGoods);
			// 修改材料库存
			Goods goods=goodsRepository.findOne(customerReturnListGoods.getGoodsId());
			goods.setInventoryQuantity(goods.getInventoryQuantity()+customerReturnListGoods.getNum());
			goods.setState(2);
			goodsRepository.save(goods);
		}
		customerReturnListRepository.save(customerReturnList); // 保存客户退货单
	}

	@Override
	public List<CustomerReturnList> list(final CustomerReturnList customerReturnList, Direction direction, String... properties) {
		return customerReturnListRepository.findAll(new Specification<CustomerReturnList>() {

			@Override
			public Predicate toPredicate(Root<CustomerReturnList> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate=cb.conjunction();
				if(customerReturnList!=null){
					if(StringUtil.isNotEmpty(customerReturnList.getCustomerReturnNumber())){
						predicate.getExpressions().add(cb.like(root.<String>get("customerReturnNumber"), "%"+customerReturnList.getCustomerReturnNumber().trim()+"%"));
					}
					if(customerReturnList.getCustomer()!=null && customerReturnList.getCustomer().getId()!=null){
						predicate.getExpressions().add(cb.equal(root.get("customer").get("id"), customerReturnList.getCustomer().getId()));
					}
					if(customerReturnList.getState()!=null){
						predicate.getExpressions().add(cb.equal(root.get("state"), customerReturnList.getState()));
					}
					if(customerReturnList.getbCustomerReturnDate()!=null){
						predicate.getExpressions().add(cb.greaterThanOrEqualTo(root.<Date>get("customerReturnDate"), customerReturnList.getbCustomerReturnDate()));
					}
					if(customerReturnList.geteCustomerReturnDate()!=null){
						predicate.getExpressions().add(cb.lessThanOrEqualTo(root.<Date>get("customerReturnDate"), customerReturnList.geteCustomerReturnDate()));
					}
				}
				return predicate;
			}
		},new Sort(direction, properties));
	}

	@Override
	public CustomerReturnList findById(Integer id) {
		return customerReturnListRepository.findOne(id);
	}

	@Transactional
	public void delete(Integer id) {
		customerReturnListGoodsRepository.deleteByCustomerReturnListId(id);
		customerReturnListRepository.delete(id);
	}

	@Override
	public void update(CustomerReturnList customerReturnList) {
		customerReturnListRepository.save(customerReturnList);
	}



}
