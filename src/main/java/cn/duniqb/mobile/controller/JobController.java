package cn.duniqb.mobile.controller;

import cn.duniqb.mobile.dto.JSONResult;
import cn.duniqb.mobile.dto.job.*;
import cn.duniqb.mobile.utils.RedisUtil;
import cn.duniqb.mobile.utils.spider.JobSpiderService;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 与就业相关的接口
 *
 * @author duniqb
 */
@Api(value = "与就业相关的接口", tags = {"与就业相关的接口"})
@RestController
@RequestMapping("/api/v1/job/")
public class JobController {

    @Autowired
    private JobSpiderService jobSpiderService;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 招聘会列表在 Redis 里的前缀
     */
    private static final String RECRUIT_LIST = "RECRUIT_LIST";


    /**
     * 招聘会在 Redis 里的前缀
     */
    private static final String RECRUIT = "RECRUIT";

    /**
     * 单位需求列表在 Redis 里的前缀
     */
    private static final String DEMAND_LIST = "DEMAND_LIST";


    /**
     * 单位需求在 Redis 里的前缀
     */
    private static final String DEMAND = "DEMAND";

    /**
     * 招聘日历在 Redis 里的前缀
     */
    private static final String CALENDAR = "CALENDAR";

    /**
     * 招聘会列表
     *
     * @param page
     * @return
     */
    @GetMapping("recruitList")
    @ApiOperation(value = "获取招聘会列表", notes = "获取招聘会列表，请求参数是 page")
    @ApiImplicitParam(name = "page", value = "页数 page", dataType = "String", paramType = "query")
    public JSONResult recruitList(@RequestParam String page) {
        String res = redisUtil.get(RECRUIT_LIST + ":" + page);
        if (res != null) {
            return JSONResult.build(JSON.parseObject(res, RecruitList.class), "招聘会列表 - 缓存获取成功", 200);
        }
        RecruitList recruitList = jobSpiderService.recruitList(page);
        if (recruitList != null) {
            redisUtil.set(RECRUIT_LIST + ":" + page, JSON.toJSONString(recruitList), 60 * 60 * 12);
            return JSONResult.build(recruitList, "招聘会列表 - 获取成功", 200);
        }
        return JSONResult.build(null, "招聘会列表 - 获取失败", 400);
    }

    /**
     * 招聘会详情
     *
     * @param id
     * @return
     */
    @GetMapping("recruit")
    @ApiOperation(value = "获取招聘会详情", notes = "获取招聘会详情，请求参数是 id")
    @ApiImplicitParam(name = "id", value = "id", dataType = "String", paramType = "query")
    public JSONResult recruit(@RequestParam String id) {
        String res = redisUtil.get(RECRUIT + ":" + id);
        if (res != null) {
            return JSONResult.build(JSON.parseObject(res, Recruit.class), "招聘会详情 - 缓存获取成功", 200);
        }
        Recruit recruit = jobSpiderService.recruit(id);
        if (recruit != null) {
            redisUtil.set(RECRUIT + ":" + id, JSON.toJSONString(recruit), 60 * 60 * 12);
            return JSONResult.build(recruit, "招聘会详情 - 获取成功", 200);
        }
        return JSONResult.build(null, "招聘会详情 - 获取失败", 400);
    }

    /**
     * 单位需求列表
     *
     * @param page
     * @return
     */
    @GetMapping("demandList")
    @ApiOperation(value = "获取单位需求列表", notes = "获取单位需求列表，请求参数是 page")
    @ApiImplicitParam(name = "page", value = "页数 page", dataType = "String", paramType = "query")
    public JSONResult demandList(@RequestParam String page) {
        String res = redisUtil.get(DEMAND_LIST + ":" + page);
        if (res != null) {
            return JSONResult.build(JSON.parseObject(res, RecruitList.class), "单位需求列表 - 缓存获取成功", 200);
        }
        DemandList demandList = jobSpiderService.demandList(page);
        if (demandList != null) {
            redisUtil.set(DEMAND_LIST + ":" + page, JSON.toJSONString(demandList), 60 * 60 * 12);
            return JSONResult.build(demandList, "单位需求列表 - 获取成功", 200);
        }
        return JSONResult.build(null, "单位需求列表 - 获取失败", 400);
    }

    /**
     * 单位需求详情
     *
     * @param id
     * @return
     */
    @GetMapping("demand")
    @ApiOperation(value = "获取单位需求详情", notes = "获取单位需求详情，请求参数是 id")
    @ApiImplicitParam(name = "id", value = "id", dataType = "String", paramType = "query")
    public JSONResult demand(@RequestParam String id) {
        String res = redisUtil.get(DEMAND + ":" + id);
        if (res != null) {
            return JSONResult.build(JSON.parseObject(res, Demand.class), "单位需求详情 - 缓存获取成功", 200);
        }
        Demand demand = jobSpiderService.demand(id);
        if (demand != null) {
            redisUtil.set(DEMAND + ":" + id, JSON.toJSONString(demand), 60 * 60 * 12);
            return JSONResult.build(demand, "单位需求详情 - 获取成功", 200);
        }
        return JSONResult.build(null, "单位需求详情 - 获取失败", 400);
    }

    /**
     * 招聘日历
     *
     * @param year
     * @return
     */
    @GetMapping("calendar")
    @ApiOperation(value = "获取招聘日历", notes = "获取招聘日历")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year", value = "年", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "month", value = "月", dataType = "String", paramType = "query")
    })
    public JSONResult calendar(@RequestParam String year, @RequestParam String month) {
        String res = redisUtil.get(CALENDAR + ":" + year + ":" + month);
        if (res != null) {
            return JSONResult.build(JSON.parseArray(res, Calendar.class), "招聘日历 - 缓存获取成功", 200);
        }
        List<Calendar> calendar = jobSpiderService.calendar(year, month);
        if (!calendar.isEmpty()) {
            redisUtil.set(CALENDAR + ":" + year + ":" + month, JSON.toJSONString(calendar), 60 * 60 * 12);
            return JSONResult.build(calendar, "招聘日历 - 获取成功", 200);
        }
        return JSONResult.build(null, "招聘日历 - 获取失败", 400);
    }
}