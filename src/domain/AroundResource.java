package domain;


/**
 * 封装周边水源信息
 * @author wsz
 * @date 2018年8月16日17:39:41
 */
public class AroundResource{

	//查询结果的整体数据
	private String content;
	//距离中心点的距离
	private double distance;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "[distance=" + distance + ", content=" + content + "]";
	}
	
}
