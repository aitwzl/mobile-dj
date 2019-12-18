package cn.duniqb.mobile.controller;

import cn.duniqb.mobile.dto.JSONResult;
import cn.duniqb.mobile.dto.repair.*;
import cn.duniqb.mobile.utils.RedisUtil;
import cn.duniqb.mobile.utils.spider.LogisticsSpiderService;
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
 * 与后勤相关的接口
 *
 * @author duniqb
 */
@Api(value = "与后勤相关的接口", tags = {"与后勤相关的接口"})
@RestController
@RequestMapping("/api/v1/logistics/")
public class LogisticsController {
    @Autowired
    private LogisticsSpiderService logisticsSpiderService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 通知查询在 Redis 里的前缀
     */
    private static final String LOGISTICS_NOTICE = "LOGISTICS_NOTICE";

    /**
     * 最新维修在 Redis 里的前缀
     */
    private static final String LOGISTICS_RECENT = "LOGISTICS_RECENT";

    /**
     * 故障报修数据在 Redis 里的前缀
     */
    private static final String LOGISTICS_DATA = "LOGISTICS_DATA";

    /**
     * 故障报修 查询各项数据清单
     */
    @GetMapping("data")
    @ApiOperation(value = "查询各项数据清单", notes = "查询各项数据清单的接口，请求参数是 id，value")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "value", value = "id 的值", required = true, dataType = "String", paramType = "query")
    })
    public JSONResult data(@RequestParam String id, @RequestParam String value) {
        String res = redisUtil.get(LOGISTICS_DATA + ":" + id + ":" + value);
        if (res != null) {
            if ("distinctId".equals(id)) {
                Buildings buildings = JSON.parseObject(res, Buildings.class);
                if (!buildings.getBuildings().isEmpty()) {
                    return JSONResult.build(buildings, "建筑物数据清单 - 缓存获取成功", 200);
                }
            } else if ("buildingId".equals(id)) {
                Rooms rooms = JSON.parseObject(res, Rooms.class);
                if (!rooms.getRooms().isEmpty()) {
                    return JSONResult.build(rooms, "房间号数据清单 - 缓存获取成功", 200);
                }
            } else if ("roomId".equals(id)) {
                Equipments equipments = JSON.parseObject(res, Equipments.class);
                if (!equipments.getEquipments().isEmpty()) {
                    return JSONResult.build(equipments, "设备号数据清单 - 缓存获取成功", 200);
                }
            } else if ("equipmentId".equals(id)) {
                Detail detail = JSON.parseObject(res, Detail.class);
                if (detail != null) {
                    return JSONResult.build(detail, "设备详情数据清单 - 缓存获取成功", 200);
                }
            }
        }
        String string = logisticsSpiderService.data(id, value);

        String replace = string.replace("\\", "");
        redisUtil.set(LOGISTICS_DATA + ":" + id + ":" + value, replace.substring(1, replace.length() - 1), 60 * 60 * 24);
        if ("distinctId".equals(id)) {
            Buildings buildings = JSON.parseObject(replace.substring(1, replace.length() - 1), Buildings.class);
            if (!buildings.getBuildings().isEmpty()) {
                return JSONResult.build(buildings, "查询建筑物数据成功", 200);
            }
        } else if ("buildingId".equals(id)) {
            Rooms rooms = JSON.parseObject(replace.substring(1, replace.length() - 1), Rooms.class);
            if (!rooms.getRooms().isEmpty()) {
                return JSONResult.build(rooms, "查询房间号数据成功", 200);
            }
        } else if ("roomId".equals(id)) {
            Equipments equipments = JSON.parseObject(replace.substring(1, replace.length() - 1), Equipments.class);
            if (!equipments.getEquipments().isEmpty()) {
                return JSONResult.build(equipments, "查询设备号数据成功", 200);
            }
        } else if ("equipmentId".equals(id)) {
            Detail detail = JSON.parseObject(replace.substring(1, replace.length() - 1), Detail.class);
            if (detail != null) {
                return JSONResult.build(detail, "查询设备详情数据成功", 200);
            }
        }
        return JSONResult.build(null, "查询数据失败", 400);
    }

    /**
     * 根据报修手机号查询报修列表
     */
    @GetMapping("list")
    @ApiOperation(value = "根据报修手机号查询报修列表", notes = "根据报修手机号查询报修列表的接口，请求参数是 phone")
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String", paramType = "query")
    public JSONResult list(@RequestParam String phone) {
        List<RepairDetail> list = logisticsSpiderService.list(phone);
        if (!list.isEmpty()) {
            return JSONResult.build(list, "查询报修列表成功", 200);
        }
        return JSONResult.build(null, "查询报修列表失败", 400);
    }

    /**
     * 报修单详情
     */
    @GetMapping("detail")
    @ApiOperation(value = "报修单详情", notes = "报修单详情的接口，请求参数是 listNumber")
    @ApiImplicitParam(name = "listNumber", value = "序列号", required = true, dataType = "String", paramType = "query")
    public JSONResult detail(@RequestParam String listNumber) {
        RepairDetail repairDetail = logisticsSpiderService.detail(listNumber);
        if (repairDetail != null) {
            return JSONResult.build(repairDetail, "查询报修单详情成功", 200);
        }
        return JSONResult.build(null, "查询报修单详情失败", 400);
    }

    /**
     * 最新通知
     */
    @GetMapping("notice")
    @ApiOperation(value = "最新通知", notes = "最新通知的接口")
    public JSONResult notice() {
        String res = redisUtil.get(LOGISTICS_NOTICE);
        if (res != null) {
            return JSONResult.build(JSON.parseObject(res, Notice.class), "最新通知 - 缓存获取成功", 200);
        }
        Notice notice = logisticsSpiderService.notice();
        if (notice != null) {
            redisUtil.set(LOGISTICS_NOTICE, JSON.toJSONString(notice), 60 * 30);
            return JSONResult.build(notice, "查询最新通知成功", 200);
        }
        return JSONResult.build(null, "查询最新通知失败", 400);
    }

    /**
     * 最近维修数量
     */
    @GetMapping("recent")
    @ApiOperation(value = "最近维修数量", notes = "最近维修数量的接口")
    public JSONResult recent() {
        String res = redisUtil.get(LOGISTICS_RECENT);
        if (res != null) {
            return JSONResult.build(JSON.parseArray(res, Recent.class), "最近维修数量 - 缓存获取成功", 200);
        }
        List<Recent> recentList = logisticsSpiderService.recent();
        if (!recentList.isEmpty()) {
            redisUtil.set(LOGISTICS_RECENT, JSON.toJSONString(recentList), 60 * 60 * 24);
            return JSONResult.build(recentList, "查询最近维修数量成功", 200);
        }
        return JSONResult.build(null, "查询最近维修数量失败", 400);
    }

    /**
     * 发起报修
     */
    @GetMapping("report")
    @ApiOperation(value = "发起报修", notes = "发起报修的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "报修电话", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "distinctId", value = "校区", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "buildingId", value = "建筑物", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "roomId", value = "房间号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "equipmentId", value = "设备", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "place", value = "房间/位置", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "description", value = "描述信息", required = true, dataType = "String", paramType = "query"),
    })
    public JSONResult report(String phone, String distinctId, String buildingId, String roomId, String equipmentId, String place, String description) {
        String listDescription = "房间号 " + place + " " + description;
        Report report = logisticsSpiderService.report(phone, distinctId, buildingId, roomId, equipmentId, listDescription);
        if (report != null) {
            return JSONResult.build(report, "发起报修成功", 200);
        }
        return JSONResult.build(null, "发起报修失败", 400);
    }

    /**
     * 维修评价
     */
    @GetMapping("evaluate")
    @ApiOperation(value = "维修评价", notes = "维修评价的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "listNumber", value = "序列号", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "phone", value = "报修电话", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "listScore", value = "打分 1-5", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "listWord", value = "评语", required = true, dataType = "String", paramType = "query")
    })
    public JSONResult evaluate(String listNumber, String phone, String listScore, String listWord) {
        logisticsSpiderService.evaluate(listNumber, phone, listScore, listWord);
        return JSONResult.build(null, "维修评价成功", 200);
    }
}
