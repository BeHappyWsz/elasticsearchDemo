package es;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import util.FileUtil;

/**
 * 更新数据
 * @author wsz
 * @date 2018年8月17日15:49:04
 */
public class PutDemo {

	public static void main(String[] args) {
		updateObj();
	}
	
	@SuppressWarnings("unchecked")
	public static void updateObj() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String uuid = FileUtil.getUUID();
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.termQuery("_id", uuid));
		SearchResponse result = GetDemo.searchAndResponse(boolQuery, true);
		if(result.getHits().getHits().length <1)//没查询出来则结束
			return ;
		SearchHit searchHit = result.getHits().getHits()[0];
		Map<String, Object> source = searchHit.getSource();
		//先获取原有的检查记录数据
		List<Map<String,Object>> jcjls = source.containsKey("JCJL") ? (List<Map<String,Object>>)source.get("JCJL") : null;
		
		//更新的参数
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("GXRY","我是更新人员");
        params.put("GXSJ",sdf.format(new Date()));
        //消火栓类型
        Map<String,Object> xhsfl = new HashMap<String,Object>();
        xhsfl.put("ID", "2");
        xhsfl.put("VALUE", "单位");
        params.put("XHSFL", xhsfl);
        //新增一条巡检记录
        Map<String,Object> jcjl = new HashMap<String,Object>();
        jcjl.put("JCJG", "我是检查机构");
    	jcjl.put("JCRY", "我是检查人员");
    	jcjl.put("JCSJ", sdf.format(new Date()));
        jcjls.add(jcjl);
        params.put("JCJL", jcjls);
        
        UpdateResponse updateResponse = null;
        int flag =0;
        try {
			UpdateRequest updateRequest = new UpdateRequest("fire_xhs_write", "xhs",uuid);
			JSONObject json = JSONObject.parseObject(JSON.toJSONString(params, SerializerFeature.WriteMapNullValue)); //SerializerFeature.WriteMapNullValue 防止当value为null时，字段会消失的情况
			updateRequest.doc(json);
			updateResponse = EsClient.getEsClient().update(updateRequest);
			flag = updateResponse.getShardInfo().getSuccessful();
			if(flag >0) {//更新成功
				Thread.sleep(1000);
				GetDemo.searchUUID(uuid);
			}else {
				System.out.println("更新失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
