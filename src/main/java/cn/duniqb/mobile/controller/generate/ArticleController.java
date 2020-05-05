package cn.duniqb.mobile.controller.generate;

import cn.duniqb.mobile.dao.ImgUrlDao;
import cn.duniqb.mobile.dto.feed.Article;
import cn.duniqb.mobile.entity.*;
import cn.duniqb.mobile.service.ArticleService;
import cn.duniqb.mobile.service.CommentService;
import cn.duniqb.mobile.service.LikeArticleService;
import cn.duniqb.mobile.service.WxUserService;
import cn.duniqb.mobile.utils.PageUtils;
import cn.duniqb.mobile.utils.R;
import cn.duniqb.mobile.utils.redis.EntityType;
import cn.duniqb.mobile.utils.redis.LikeService;
import cn.duniqb.mobile.utils.redis.RedisKeyUtil;
import cn.duniqb.mobile.utils.redis.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;


/**
 * 文章表
 *
 * @author duniqb
 * @email duniqb@qq.com
 * @date 2020-05-04 09:28:06
 */
@Slf4j
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private LikeArticleService likeArticleService;

    @Autowired
    private CommentService commentService;

    @Resource
    private ImgUrlDao imgUrlDao;

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private LikeService likeService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("mobile:article:list")
    public R list(@RequestParam Map<String, Object> params, @RequestParam String sessionId) {
        PageUtils page = articleService.queryPage(params);


        List<ArticleEntity> articleEntityList = (List<ArticleEntity>) page.getList();

        List<Article> articleList = new ArrayList<>();

        for (ArticleEntity articleEntity : articleEntityList) {
            Article article = new Article();

            // 复制已有属性
            BeanUtils.copyProperties(articleEntity, article);

            // 过长的文字截断
            if (article.getContent().length() > 90) {
                article.setContent(article.getContent().substring(0, 90) + "...");
            }

            // 查找该文章可能关联的图片
            QueryWrapper<ImgUrlEntity> queryWrapperImg = new QueryWrapper<>();
            queryWrapperImg.eq("article_id", article.getId());
            List<ImgUrlEntity> imgUrlEntityList = imgUrlDao.selectList(queryWrapperImg);

            List<String> imgList = new ArrayList<>();
            for (ImgUrlEntity imgUrlEntity : imgUrlEntityList) {
                imgList.add(imgUrlEntity.getUrl());
            }
            article.setImgUrlList(imgList);

            // 查找文章的作者名
            QueryWrapper<WxUserEntity> queryWrapperName = new QueryWrapper<>();
            queryWrapperName.eq("openid", article.getOpenId());
            WxUserEntity wxUser = wxUserService.getOne(queryWrapperName);
            article.setAuthor(wxUser.getNickname());
            article.setAvatar(wxUser.getAvatarUrl());

            // 查找点赞数量，先查看Redis 里是否有该文章的点赞情况存储，如果有则返回，没有则从数据库查询后写入redis并返回
            String sessionIdValue = redisUtil.get(sessionId);
            if (sessionIdValue != null) {
                String openid = sessionIdValue.split(":")[0];

                String likeKey = RedisKeyUtil.getLikeKey(EntityType.ENTITY_ARTICLE, article.getId());
                // Redis 没有该文章的点赞记录
                if (redisUtil.scard(likeKey) == 0) {
                    QueryWrapper<LikeArticleEntity> queryWrapperLike = new QueryWrapper<>();
                    queryWrapperLike.eq("article_id", article.getId());
                    List<LikeArticleEntity> likeArticleEntityList = likeArticleService.list(queryWrapperLike);
                    // 把数据库存在的该文章的点赞记录放置到 Redis
                    if (!likeArticleEntityList.isEmpty()) {
                        for (LikeArticleEntity likeArticleEntity : likeArticleEntityList) {
                            System.out.println("写入了点赞记录");
                            likeService.like(likeArticleEntity.getOpenId(), EntityType.ENTITY_ARTICLE, article.getId());
                        }
                    }
                }

                // 该用户是否对该文章点赞
                Boolean likeStatus = likeService.getLikeStatus(openid, EntityType.ENTITY_ARTICLE, article.getId());
                if (likeStatus) {
                    article.setIsLike(true);
                }

                article.setLikeCount((likeService.getLikeCount(EntityType.ENTITY_ARTICLE, article.getId())));
            }


            // 查找评论数量
            QueryWrapper<CommentEntity> queryWrapperComment = new QueryWrapper<>();
            queryWrapperComment.eq("article_id", articleEntity.getId());
            List<CommentEntity> commentEntityList = commentService.list(queryWrapperComment);
            article.setCommentCount(commentEntityList.size());

            articleList.add(article);
        }

        page.setList(articleList);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("mobile:article:info")
    public R info(@PathVariable("id") Integer id) {
        ArticleEntity articleEntity = articleService.getById(id);

        Article article = new Article();

        // 复制已有属性
        BeanUtils.copyProperties(articleEntity, article);

        // 查找该文章可能关联的图片
        Integer articleId = articleEntity.getId();
        QueryWrapper<ImgUrlEntity> queryWrapperImg = new QueryWrapper<>();
        queryWrapperImg.eq("article_id", articleId);
        List<ImgUrlEntity> imgUrlEntityList = imgUrlDao.selectList(queryWrapperImg);

        List<String> imgList = new ArrayList<>();
        for (ImgUrlEntity imgUrlEntity : imgUrlEntityList) {
            imgList.add(imgUrlEntity.getUrl());
        }
        article.setImgUrlList(imgList);

        // 查找文章的作者名
        QueryWrapper<WxUserEntity> queryWrapperName = new QueryWrapper<>();
        queryWrapperImg.eq("openid", articleEntity.getOpenId());
        WxUserEntity wxUser = wxUserService.getOne(queryWrapperName);
        article.setAuthor(wxUser.getNickname());
        article.setAvatar(wxUser.getAvatarUrl());

        return R.ok().put("article", article);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("mobile:article:save")
    public R save(@RequestBody ArticleEntity article) {
        articleService.save(article);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("mobile:article:update")
    public R update(@RequestBody ArticleEntity article) {
        articleService.updateById(article);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("mobile:article:delete")
    public R delete(@RequestBody Integer[] ids) {
        articleService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
