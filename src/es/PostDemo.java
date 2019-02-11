package es;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import util.FileUtil;

/**
 * 添加/新增
 * @author wsz
 * @date 2018年8月17日11:12:26
 */

public class PostDemo {
	
	public static void main(String[] args) throws Exception {
		postObject();
	}
	
	/**
	 * @throws IOException 
	 * @throws InterruptedException 
	 * 
	 */
	public static void postObject() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String uuid = UUID.randomUUID().toString().replace("-", "");
		Map<String,Object> objMap = new HashMap<String,Object>();
		objMap.put("KYZT", "0");//默认不可用,防止与正常数据冲突
		objMap.put("WD", 25.00);
		objMap.put("JD", 120.00);
		objMap.put("XHSBH", uuid);
		objMap.put("XHSDZ", "我是消火栓地址");
		objMap.put("RKSJ", sdf.format(new Date()));
		objMap.put("GXSJ", sdf.format(new Date()));
		//对象型(key-value):消火栓分类 1市政2单位3居民
		Map<String,Object> xfsfl=new HashMap<String,Object>();
        xfsfl.put("ID","1");
        xfsfl.put("VALUE","市政");
        objMap.put("XHSFL",xfsfl);
		//List格式:检查记录JCJL(JCJG-JCRY-JCSJ)
        List<Map<String,Object>> jcjls = new ArrayList<Map<String,Object>>();
        for(int i=0; i<3; i++) {
        	Map<String,Object> jcjl=new HashMap<String,Object>();
        	jcjl.put("JCJG", "我是检查机构");
        	jcjl.put("JCRY", "我是检查人员");
        	jcjl.put("JCSJ", sdf.format(new Date()));
        	jcjls.add(jcjl);
        }
        objMap.put("JCJL", jcjls);
        
		IndexRequest request = new IndexRequest("fire_xhs_write","xhs", uuid).source(objMap);
		IndexResponse response = EsClient.getEsClient().index(request);
		if(DocWriteResponse.Result.CREATED == response.getResult()) {
			//保存到本地
			System.out.println(FileUtil.insertUUID(uuid));
			//延时查询,否则查询不到
			Thread.sleep(1000);
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			boolQuery.must(QueryBuilders.termQuery("_id", uuid));
			GetDemo.searchAndResponse(boolQuery, true);
		}else {
			System.out.println("新增失败");
		}
	}
}
