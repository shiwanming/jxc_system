package com.mf.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mf.entity.Goods;
import com.mf.entity.Log;
import com.mf.service.CustomerReturnListGoodsService;
import com.mf.service.GoodsService;
import com.mf.service.LogService;
import com.mf.service.SaleListGoodsService;
import com.mf.util.StringUtil;

/**
 * 后台管理材料Controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/admin/goods")
public class GoodsAdminController {

	@Resource
	private GoodsService goodsService;

	@Resource
	private SaleListGoodsService saleListGoodsService;

	@Resource
	private CustomerReturnListGoodsService customerReturnListGoodsService;

	@Resource
	private LogService logService;

	/**
	 * 分页查询材料信息
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/list")
	@RequiresPermissions(value={"材料管理","进货入库","退货出库","销售出库","客户退货","材料报损","材料报溢"},logical=Logical.OR)
	public Map<String,Object> list(Goods goods,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="rows",required=false)Integer rows)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		List<Goods> goodsList=goodsService.list(goods, page, rows, Direction.ASC, "id");
		Long total=goodsService.getCount(goods);
		resultMap.put("rows", goodsList);
		resultMap.put("total", total);
		logService.save(new Log(Log.SEARCH_ACTION,"查询材料库存信息"));
		return resultMap;
	}

	/**
	 * 查询库存报警材料
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/listAlarm")
	@RequiresPermissions(value="库存报警")
	public Map<String,Object> listAlarm()throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		resultMap.put("rows", goodsService.listAlarm());
		logService.save(new Log(Log.SEARCH_ACTION,"查询库存报警材料信息"));
		return resultMap;
	}


	/**
	 * 根据条件分页查询材料库存信息
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/listInventory")
	@RequiresPermissions(value="当前库存查询")
	public Map<String,Object> listInventory(Goods goods,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="rows",required=false)Integer rows)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		List<Goods> goodsList=goodsService.list(goods, page, rows, Direction.ASC, "id");
		for(Goods g:goodsList){
			g.setSaleTotal(saleListGoodsService.getTotalByGoodsId(g.getId())-customerReturnListGoodsService.getTotalByGoodsId(g.getId())); // 设置销售总量
		}
		Long total=goodsService.getCount(goods);
		resultMap.put("rows", goodsList);
		resultMap.put("total", total);
		logService.save(new Log(Log.SEARCH_ACTION,"查询材料信息"));
		return resultMap;
	}

	/**
	 * 根据条件分页查询没有库存的材料信息
	 * @param codeOrName
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/listNoInventoryQuantity")
	@RequiresPermissions(value="期初库存")
	public Map<String,Object> listNoInventoryQuantity(@RequestParam(value="codeOrName",required=false)String codeOrName,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="rows",required=false)Integer rows)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		List<Goods> goodsList=goodsService.listNoInventoryQuantityByCodeOrName(codeOrName, page, rows, Direction.ASC, "id");
		Long total=goodsService.getCountNoInventoryQuantityByCodeOrName(codeOrName);
		resultMap.put("rows", goodsList);
		resultMap.put("total", total);
		logService.save(new Log(Log.SEARCH_ACTION,"查询材料信息(无库存)"));
		return resultMap;
	}

	/**
	 * 根据条件分页查询有库存的材料信息
	 * @param codeOrName
	 * @param page
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/listHasInventoryQuantity")
	@RequiresPermissions(value="期初库存")
	public Map<String,Object> listHasInventoryQuantity(@RequestParam(value="codeOrName",required=false)String codeOrName,@RequestParam(value="page",required=false)Integer page,@RequestParam(value="rows",required=false)Integer rows)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		List<Goods> goodsList=goodsService.listHasInventoryQuantityByCodeOrName(codeOrName, page, rows, Direction.ASC, "id");
		Long total=goodsService.getCountHasInventoryQuantityByCodeOrName(codeOrName);
		resultMap.put("rows", goodsList);
		resultMap.put("total", total);
		logService.save(new Log(Log.SEARCH_ACTION,"查询材料信息(有库存)"));
		return resultMap;
	}

	/**
	 * 生成材料编码
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/genGoodsCode")
	@RequiresPermissions(value="材料管理")
	public String genGoodsCode()throws Exception{
		String maxGoodsCode=goodsService.getMaxGoodsCode();
		if(StringUtil.isNotEmpty(maxGoodsCode)){
			Integer code=Integer.parseInt(maxGoodsCode)+1;
			String codes=code.toString();
			int length=codes.length();
			for(int i=4;i>length;i--){
				codes="0"+codes;
			}
			return codes;
		}else{
			return "0001";
		}
	}

	/**
	 * 添加或者修改材料信息
	 * @param goods
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/save")
	@RequiresPermissions(value="材料管理")
	public Map<String,Object> save(Goods goods)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		if(goods.getId()!=null){
			logService.save(new Log(Log.UPDATE_ACTION,"更新材料信息"+goods));
		}else{
			logService.save(new Log(Log.ADD_ACTION,"添加材料信息"+goods));
			goods.setLastPurchasingPrice(goods.getPurchasingPrice()); // 设置上次进价为当前价格
		}
		goodsService.save(goods);
		resultMap.put("success", true);
		return resultMap;
	}

	/**
	 * 删除材料信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/delete")
	@RequiresPermissions(value="材料管理")
	public Map<String,Object> delete(Integer id)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		Goods goods=goodsService.findById(id);
		if(goods.getState()==1){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "该材料已经期初入库，不能删除");
		}else if(goods.getState()==2){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "该材料已经发生单据，不能删除");
		}else{
			logService.save(new Log(Log.DELETE_ACTION,"删除材料信息"+goods));
			goodsService.delete(id);
			resultMap.put("success", true);
		}
		return resultMap;
	}

	/**
	 * 添加材料到仓库 修改库存以及价格信息
	 * @param id
	 * @param num
	 * @param price
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/saveStore")
	@RequiresPermissions(value="期初库存")
	public Map<String,Object> saveStore(Integer id,Integer num,Float price)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		Goods goods=goodsService.findById(id);
		goods.setInventoryQuantity(num);
		goods.setPurchasingPrice(price);
		goods.setLastPurchasingPrice(price);
		goodsService.save(goods);
		logService.save(new Log(Log.UPDATE_ACTION,"修改材料信息："+goods+"，价格="+price+",库存="+num));
		resultMap.put("success", true);
		return resultMap;
	}

	/**
	 * 删除库存，吧材料的库存设置为0
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/deleteStock")
	@RequiresPermissions(value="期初库存")
	public Map<String,Object> deleteStock(Integer id)throws Exception{
		Map<String,Object> resultMap=new HashMap<>();
		Goods goods=goodsService.findById(id);
		if(goods.getState()==2){
			resultMap.put("success", false);
			resultMap.put("errorInfo", "该材料已经发生单据，不能删除");
		}else{
			goods.setInventoryQuantity(0);
			goodsService.save(goods);
			logService.save(new Log(Log.UPDATE_ACTION,"修改材料信息"+goods));
			resultMap.put("success", true);
		}
		return resultMap;
	}

}
