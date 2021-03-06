package com.mf.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mf.entity.GoodsUnit;
import com.mf.entity.Log;
import com.mf.service.GoodsUnitService;
import com.mf.service.LogService;

/**
 * 后台管理材料单位Controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/admin/goodsUnit")
public class GoodsUnitAdminController {


	@Resource
	private GoodsUnitService goodsUnitService;

	@Resource
	private LogService logService;

	/**
	 * 返回所有材料单位 下拉框用到
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/comboList")
	@RequiresPermissions(value="材料管理")
	public List<GoodsUnit> comboList()throws Exception{
		return goodsUnitService.listAll();
	}

	/**
	 * 查询所有材料单位信息
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/listAll")
	@RequiresPermissions(value="材料管理")
	public Map<String,Object> listAll()throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		resultMap.put("rows", goodsUnitService.listAll());
		logService.save(new Log(Log.SEARCH_ACTION,"查询材料单位信息"));
		return resultMap;
	}

	/**
	 * 添加材料单位
	 * @param goodsUnit
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/save")
	@RequiresPermissions(value="材料管理")
	public Map<String,Object> save(GoodsUnit goodsUnit)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		goodsUnitService.save(goodsUnit);
		logService.save(new Log(Log.ADD_ACTION,"添加材料单位信息"));
		resultMap.put("success", true);
		return resultMap;
	}

	/**
	 * 材料单位删除
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/delete")
	@RequiresPermissions(value="材料管理")
	public Map<String,Object> delete(Integer id)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		logService.save(new Log(Log.DELETE_ACTION,"删除材料单位信息："+goodsUnitService.findById(id)));
		goodsUnitService.delete(id);
		resultMap.put("success", true);
		return resultMap;
	}


}
