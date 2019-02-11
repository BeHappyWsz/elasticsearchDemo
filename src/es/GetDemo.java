package es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.vividsolutions.jts.geom.Coordinate;

import domain.AroundResource;
import util.ComConvert;
import util.ComMethod;

/**
 * 
 * @author wsz
 * @date 2018年8月16日15:58:41
 */
public class GetDemo {
	
	public static void main(String[] args) throws Exception {
//		rectangleSearch();
//		circleSearch();
//		polygonSearch();
//		pointSearch();
		wildSearch();
	}
	
	/**
	 * 矩形坐标范围内查询
	 * topLeft 左上角坐标点
	 * bottomRight 右下角坐标点
	 * @throws IOException 
	 */
	public static void rectangleSearch() throws IOException {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		Coordinate topLeft = new Coordinate(113.2493947644, 23.1538346445);
		Coordinate bottomRight = new Coordinate(113.2531274313, 23.1480155791);
		boolQuery.must(QueryBuilders.geoShapeQuery("SHAPE", ShapeBuilders.newEnvelope(topLeft, bottomRight)));
		searchAndResponse(boolQuery,true);
	}
	/**
	 * 根据中心点和单位范围内进行查询
	 * @throws IOException 
	 */
	public static void circleSearch() throws IOException {
		//圆心坐标与半径
		double center_lng = 113.2493947644;
		double center_lat = 23.1538346445;
		String radius = "1000m";
		
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.geoShapeQuery("SHAPE",ShapeBuilders.newCircleBuilder().center(center_lng, center_lat).radius(radius)));
		SearchResponse response = searchAndResponse(boolQuery, false);
		if(response != null) {
			SearchHits hits = response.getHits();
	        SearchHit[] searchHits = hits.getHits();
	        Set<AroundResource> s = new TreeSet<AroundResource>(new Comparator<AroundResource>() {
				@Override
				public int compare(AroundResource o1, AroundResource o2) {
					return o1.getDistance() >= o2.getDistance() ? 1 : -1;
				}
	        });
	        for(SearchHit hit : searchHits) {
	        	Map<String, Object> source = hit.getSource();
	        	AroundResource ar = new AroundResource();
	        	ar.setContent(source.get("XHSMC").toString());
	        	//离中心点的距离
	        	double jd = source.containsKey("JD") ? ComConvert.toDouble(source.get("JD"), 0.0) : 0.0;
				double wd = source.containsKey("WD") ? ComConvert.toDouble(source.get("WD"), 0.0) : 0.0;
				ar.setDistance(ComMethod.getSphericalDistance(center_lng, center_lat, jd, wd));
				s.add(ar);
	        }
	        for(AroundResource temp :s) {
	        	System.out.println(temp.toString());
	        }
		}
	}
	
	/**
	 * 不规则多边形内部查询
	 * @throws IOException 
	 */
	public static void polygonSearch() throws IOException {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		//长度大于4,
		List<Coordinate> shell = new ArrayList<Coordinate>();
//		shell.add(new Coordinate(114.05569610096002, 22.828212691385414));
//		shell.add(new Coordinate(114.05568611167995, 22.82804275878187));
//		shell.add(new Coordinate(114.05559620815994, 22.82786295603078));
//		shell.add(new Coordinate(114.0554763368, 22.82774318247331));
//		shell.add(new Coordinate(114.055376444, 22.827673361987205));
//		shell.add(new Coordinate(114.05525657390322, 22.827643561409644));
//		shell.add(new Coordinate(114.05514669056, 22.827663729331288));
//		shell.add(new Coordinate(114.05502681919997, 22.827773886645847));
//		shell.add(new Coordinate(114.05504679776001, 22.827913813212536));
//		shell.add(new Coordinate(114.05506677632006, 22.828063736753762));
//		shell.add(new Coordinate(114.05503680848003, 22.828123766486502));
//		shell.add(new Coordinate(114.05490694784001, 22.82816396099458));
//		shell.add(new Coordinate(114.05488696928005, 22.82816399275291));
//		shell.add(new Coordinate(114.0547271408, 22.82810426455843));
//		shell.add(new Coordinate(114.05441747311997, 22.82810475666976));
//		shell.add(new Coordinate(114.05409781616004, 22.8281252588395));
//		shell.add(new Coordinate(114.05400791263995, 22.828195381416133));
//		shell.add(new Coordinate(114.05399792336006, 22.828355350890956));
//		shell.add(new Coordinate(114.0540678483201, 22.82847520476507));
//		shell.add(new Coordinate(114.05434754816001, 22.82858472728146));
//		shell.add(new Coordinate(114.05463723727999, 22.82858426585021));
//		shell.add(new Coordinate(114.05529652976007, 22.82850323969829));
//		shell.add(new Coordinate(114.05562617599998, 22.82834276323364));
//		shell.add(new Coordinate(114.05569610096002, 22.828212691385414));
		boolQuery.must(QueryBuilders.geoShapeQuery("SHAPE", ShapeBuilders.newPolygon(shell)));
		//查询
		searchAndResponse(boolQuery,true);
	}
	
	/**
	 * 判断点坐标是否存在信息
	 * @throws IOException
	 */
	public static void pointSearch() throws IOException {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.geoShapeQuery("SHAPE", ShapeBuilders.newPoint(113.2531100217, 23.1517177492)));
		searchAndResponse(boolQuery,true);
	}
	
	/**
	 * 模糊查询
	 */
	public static void wildSearch() {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.termQuery("JLZT", "1"));//0无效1有效
		//模糊查询,不匹配则为空
		BoolQueryBuilder wildQuery = QueryBuilders.boolQuery();
		String searchStr = "*消火栓*";
		wildQuery.must(QueryBuilders.wildcardQuery("XHSMC", searchStr));
		wildQuery.must(QueryBuilders.wildcardQuery("XHSDZ", searchStr));
		boolQuery.must(wildQuery);
		
		searchAndResponse(boolQuery,true);
	}
	/**
	 * 根据uuid主键查询
	 * @param uuid
	 * @return
	 */
	public static SearchResponse searchUUID(String uuid) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(QueryBuilders.termQuery("_id", uuid));
		return GetDemo.searchAndResponse(boolQuery, true);
	}
	/**
	 * 通用的查询方法
	 * @param boolQuery	查询语句对象
	 * @param showFlag 是否打印结果信息
	 * @return	
	 */
	public static SearchResponse searchAndResponse(BoolQueryBuilder boolQuery, boolean showFlag) {
		SearchResponse response = null;
		try {
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
	                .timeout(new TimeValue(60, TimeUnit.SECONDS))//查询超时时间
	                .from(0)           //起始
	                .size(10)          //数据量
	                .query(boolQuery); //查询
	        SearchRequest searchRequest = new SearchRequest()
	                .indices("fire_xhs_read")	//索引名
	                .types("xhs")				//类型
	                .source(searchSourceBuilder);
	        response = EsClient.getEsClient().search(searchRequest);
		} catch (IOException e) {
			e.printStackTrace();
		};
		
		if(response!= null && showFlag) {
			SearchHits hits = response.getHits();
	        SearchHit[] searchHits = hits.getHits();
	        for(SearchHit hit : searchHits) {
	        	Map<String, Object> source = hit.getSource();
	        	System.out.println(source.toString());
	        }
		}
		return response;
	}
}
