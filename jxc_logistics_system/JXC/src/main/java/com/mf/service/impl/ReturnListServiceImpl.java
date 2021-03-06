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
import com.mf.entity.ReturnList;
import com.mf.entity.ReturnListGoods;
import com.mf.repository.GoodsRepository;
import com.mf.repository.GoodsTypeRepository;
import com.mf.repository.ReturnListGoodsRepository;
import com.mf.repository.ReturnListRepository;
import com.mf.service.ReturnListService;
import com.mf.util.StringUtil;

/**
 * 退货单Service实现类
 * @author Administrator
 *
 */
@Service("returnListService")
public class ReturnListServiceImpl implements ReturnListService{

	@Resource
	private ReturnListRepository returnListRepository;

	@Resource
	private GoodsTypeRepository goodsTypeRepository;

	@Resource
	private GoodsRepository goodsRepository;

	@Resource
	private ReturnListGoodsRepository returnListGoodsRepository;

	@Override
	public String getTodayMaxReturnNumber() {
		return returnListRepository.getTodayMaxReturnNumber();
	}

	@Transactional
	public void save(ReturnList returnList, List<ReturnListGoods> returnListGoodsList) {
		for(ReturnListGoods returnListGoods:returnListGoodsList){
			returnListGoods.setType(goodsTypeRepository.findOne(returnListGoods.getTypeId())); // 设置类别
			returnListGoods.setReturnList(returnList); // 设置退货单
			returnListGoodsRepository.save(returnListGoods);
			// 修改材料库存 成本均价 以及上次进价
			Goods goods=goodsRepository.findOne(returnListGoods.getGoodsId());

			goods.setInventoryQuantity(goods.getInventoryQuantity()-returnListGoods.getNum());
			goods.setState(2);
			goodsRepository.save(goods);
		}
		returnListRepository.save(returnList); // 保存退货单
	}

	@Override
	public ReturnList findById(Integer id) {
		return returnListRepository.findOne(id);
	}

	@Override
	public List<ReturnList> list(final ReturnList returnList, Direction direction, String... properties) {
		return returnListRepository.findAll(new Specification<ReturnList>() {

				@Override
				public Predicate toPredicate(Root<ReturnList> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					Predicate predicate=cb.conjunction();
					if(returnList!=null){
						if(StringUtil.isNotEmpty(returnList.getReturnNumber())){
							predicate.getExpressions().add(cb.like(root.<String>get("returnNumber"), "%"+returnList.getReturnNumber().trim()+"%"));
						}
						if(returnList.getSupplier()!=null && returnList.getSupplier().getId()!=null){
							predicate.getExpressions().add(cb.equal(root.get("supplier").get("id"), returnList.getSupplier().getId()));
						}
						if(returnList.getState()!=null){
							predicate.getExpressions().add(cb.equal(root.get("state"), returnList.getState()));
						}
						if(returnList.getbReturnDate()!=null){
							predicate.getExpressions().add(cb.greaterThanOrEqualTo(root.<Date>get("returnDate"), returnList.getbReturnDate()));
						}
						if(returnList.geteReturnDate()!=null){
							predicate.getExpressions().add(cb.lessThanOrEqualTo(root.<Date>get("returnDate"), returnList.geteReturnDate()));
						}
					}
					return predicate;
				}
			},new Sort(direction, properties));
	}

	@Transactional
	public void delete(Integer id) {
		returnListGoodsRepository.deleteByReturnListId(id);
		returnListRepository.delete(id);
	}

	@Override
	public void update(ReturnList returnList) {
		returnListRepository.save(returnList);
	}



}
