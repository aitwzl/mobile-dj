package cn.duniqb.mobile.service;

import cn.duniqb.mobile.nosql.mongodb.document.feed.Title;
import com.mongodb.client.result.DeleteResult;

import java.util.List;

public interface FeedService {

    /**
     * 创建文章
     *
     * @param title
     * @return
     */
    Title save(Title title);

    /**
     * 根据 id 删除文章
     *
     * @param id
     * @return
     */
    DeleteResult delete(String id);

    /**
     * 分页查询文章
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<Title> listDesc(int pageNum, int pageSize);

    /**
     * 根据 id 查询文章
     *
     * @param id
     * @return
     */
    Title findById(String id);
}
