package es;

import java.io.IOException;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;

import util.FileUtil;

/**
 * 删除案例
 * @author wsz
 * @date 2018年8月17日16:38:19
 */
public class DeleteDemo {

	public static void main(String[] args) throws Exception {
		deleteObj();
	}
	
	public static void deleteObj() throws IOException {
		String uuid = FileUtil.getUUID();
		if("empty".equals(uuid)) {
			System.out.println("暂无数据删除");
			return;
		}
		System.out.println("即将删除:"+uuid);
		DeleteRequest delete = new DeleteRequest("fire_xhs_write", "xhs",uuid);
		DeleteResponse response = EsClient.getEsClient().delete(delete);
		if(response.getResult() == DocWriteResponse.Result.DELETED) {
			FileUtil.deleteUUID(uuid);
			System.out.println("删除成功");
		}else {
			System.out.println("删除失败");
		}
	}
}
