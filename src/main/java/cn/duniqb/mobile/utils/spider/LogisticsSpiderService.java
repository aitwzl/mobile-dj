package cn.duniqb.mobile.utils.spider;

import cn.duniqb.mobile.dto.repair.*;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 与后勤相关的接口
 */
@Service
public class LogisticsSpiderService {
    /**
     * 查询校区 id，返回建筑 id的 url
     */
    @Value("${logistics.data.distinctIdUrl}")
    private String distinctIdUrl;

    /**
     * 查询建筑 id，返回房间 id的 url
     */
    @Value("${logistics.data.buildingIdUrl}")
    private String buildingIdUrl;

    /**
     * 查询房间 id，返回设备 id的 url
     */
    @Value("${logistics.data.roomIdUrl}")
    private String roomIdUrl;

    /**
     * 查询设备 id，返回设备详情的 url
     */
    @Value("${logistics.data.equipmentIdUrl}")
    private String equipmentIdUrl;

    /**
     * 根据报修手机号查询报修列表的 url
     */
    @Value("${logistics.detail.listUrl}")
    private String listUrl;

    /**
     * 报修网站的主机地址
     */
    @Value("${logistics.logisticsHost}")
    private String logisticsHost;

    /**
     * 最新通知的 url
     */
    @Value("${logistics.noticeUrl}")
    private String noticeUrl;

    /**
     * 最近维修数量的 url
     */
    @Value("${logistics.recentUrl}")
    private String recentUrl;

    /**
     * 发起报修的 url
     */
    @Value("${logistics.reportUrl}")
    private String reportUrl;

    /**
     * 评价的 url
     */
    @Value("${logistics.evaluateUrl}")
    private String evaluateUrl;

    /**
     * 故障报修 查询各项数据清单
     */
    public String data(String id, String value) {
        String url = null;
        // 查询校区 id，返回建筑 id
        if ("distinctId".equals(id)) {
            url = distinctIdUrl;
        }
        // 查询建筑 id，返回房间 id
        else if ("buildingId".equals(id)) {
            url = buildingIdUrl;
        }
        // 查询房间 id，返回设备 id
        else if ("roomId".equals(id)) {
            url = roomIdUrl;
        }
        // 查询设备 id，返回设备详情
        else if ("equipmentId".equals(id)) {
            url = equipmentIdUrl;
        }

        HttpResponse response;
        HttpClient client = HttpClients.createDefault();

        try {
            ArrayList<NameValuePair> postData = new ArrayList<>();
            postData.add(new BasicNameValuePair(id, value));

            HttpPost post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(postData));

            post.setHeader("Accept", "application/json");
            post.setHeader("Accept-Encoding", "gzip, deflate, br");
            post.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            post.setHeader("Connection", "keep-alive");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            post.setHeader("Host", "nanqu.56team.com");
            post.setHeader("Sec-Fetch-Mode", "cors");
            post.setHeader("Sec-Fetch-Site", "same-origin");
            post.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
            post.setHeader("X-Requested-With", "XMLHttpRequest");

            response = client.execute(post);
            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", ""));
            return JSON.toJSONString(doc.text());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据报修手机号查询报修列表
     *
     * @param userTel
     */
    public List<RepairDetail> list(String userTel) {
        HttpResponse response;
        HttpClient client = HttpClients.createDefault();
        List<RepairDetail> list = new ArrayList<>();
        try {
            ArrayList<NameValuePair> postData = new ArrayList<>();
            postData.add(new BasicNameValuePair("userTel", userTel));

            HttpPost post = new HttpPost(listUrl);

            post.setEntity(new UrlEncodedFormEntity(postData));

            // 重要的 Header
            post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            post.setHeader("Accept-Encoding", "gzip, deflate, br");
            post.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
            post.setHeader("Connection", "keep-alive");
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setHeader("Host", "nanqu.56team.com");
            post.setHeader("Sec-Fetch-Mode", "navigate");
            post.setHeader("Sec-Fetch-Site", "same-origin");
            post.setHeader("Sec-Fetch-User", "?1");
            post.setHeader("Upgrade-Insecure-Requests", "1");
            post.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
            post.setHeader("X-Requested-With", "XMLHttpRequest");

            response = client.execute(post);
            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", ""));
            Elements elements = doc.select("div.page-current div.content>a");

            for (int i = 0; i < elements.size(); i++) {
                RepairDetail repairDetail = new RepairDetail();
                if (elements.get(i).select("form input[value]").last() != null) {
                    repairDetail.setShowEvaluate(true);
                } else {
                    repairDetail.setShowEvaluate(false);
                }
                repairDetail.setPhone(userTel);
                // 报修时间
                repairDetail.setDate(elements.get(i).select("p.color-gray").text().split("\\.")[0]);
                // 标题
                repairDetail.setTitle(elements.get(i).select("a .card-content-inner>p[style]").last().text());
                // 报修单号
                repairDetail.setId(elements.get(i).select("div.color-gray").text().split(":")[1]);
                // 提交状态
                repairDetail.setState(elements.get(i).select("div.card-content div[style] div[style] p").text());
                // 链接
                repairDetail.setListNumber(elements.get(i).select("a").attr("href").split("/")[6].split("\\.")[0]);

                list.add(repairDetail);
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 报修单详情
     */
    public RepairDetail detail(String listNumber) {
        HttpGet detailGet = new HttpGet(logisticsHost + "/web/app/user/select/oneMaintenance/" + listNumber + ".action");

        detailGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        detailGet.setHeader("Accept-Encoding", "gzip, deflate");
        detailGet.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        detailGet.setHeader("Connection", "keep-alive");
        detailGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
        HttpClient client = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            response = client.execute(detailGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        if (response.toString().contains("200")) {

            try {
                Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", "").replace("amp;", ""));
                Element element = doc.select("div.page-current div.card-content-inner").first();

                RepairDetail repairDetail = new RepairDetail();
                if (element.select("form input[value]").last() != null) {
                    repairDetail.setShowEvaluate(true);
                } else {
                    repairDetail.setShowEvaluate(false);
                }
                repairDetail.setDate(element.select("p.color-gray").text().split("\\.")[0]);
                if(element.select("p[style]").get(0).text().contains("_")) {
                    repairDetail.setTitle(element.select("p[style]").first().text());
                } else {
                    repairDetail.setTitle(element.select("p[style]").get(2).text());
                }
                repairDetail.setRoom(element.select("div.card-content-inner>p[style]").last().text().split(" ")[1]);
                repairDetail.setDescription(element.select("div.card-content-inner>p[style]").last().text().split(" ")[2]);
                repairDetail.setId(element.select("div.color-gray").text().split(":")[1]);
                repairDetail.setState(element.select("div.card-content-inner>div[style] p").text());
                repairDetail.setListNumber(listNumber);
                List<TimeLine> timeLineList = new ArrayList<>();

                Elements timelineElement = doc.select("div.page-current div.card-content-inner").last().select("ul li");
                for (int i = 0; i < timelineElement.size(); i++) {
                    TimeLine timeLine = new TimeLine();
                    timeLine.setTime(timelineElement.get(i).select(".item-title").text().split("\\.")[0]);
                    timeLine.setComment(timelineElement.get(i).select(".item-after").text());
                    timeLineList.add(timeLine);
                }
                repairDetail.setTimeLineList(timeLineList);
                return repairDetail;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 最新通知
     */
    public Notice notice() {
        HttpGet noticeGet = new HttpGet(noticeUrl);

        noticeGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        noticeGet.setHeader("Accept-Encoding", "gzip, deflate");
        noticeGet.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        noticeGet.setHeader("Connection", "keep-alive");
        noticeGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        HttpResponse response = null;
        HttpClient client = HttpClients.createDefault();

        try {
            response = client.execute(noticeGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null && !response.toString().contains("200")) {
            return null;
        }

        try {
            assert response != null;
            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", "").replace("amp;", ""));
            Element element = doc.select("div.bottom_inside a").first();

            Notice notice = new Notice();
            notice.setTitle(element.select(".bottom_inside_title").text());
            notice.setContent(element.select(".bottom_inside_art").text());
            notice.setDate(element.select(".inscription").text());

            return notice;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 最近维修数量
     */
    public List<Recent> recent() {
        HttpGet recentGet = new HttpGet(recentUrl);

        recentGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        recentGet.setHeader("Accept-Encoding", "gzip, deflate");
        recentGet.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        recentGet.setHeader("Connection", "keep-alive");
        recentGet.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        HttpResponse response = null;
        HttpClient client = HttpClients.createDefault();
        try {
            response = client.execute(recentGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Recent> list = new ArrayList<>();
        try {
            assert response != null;
            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", "").replace("amp;", ""));
            Elements elements = doc.select("div.content>div.list-block>ul>li");
            for (int i = 1; i < elements.size(); i++) {
                Recent recent = new Recent();
                recent.setArea(elements.get(i).select(".item-title").text());
                String reported = elements.get(i).select(".item-after").text().split("/")[0].split("条")[0];
                recent.setReported(reported);
                String repaired = elements.get(i).select(".item-after").text().split("/")[1].split("条")[0];
                recent.setRepaired(repaired);
                recent.setPending(String.valueOf(Integer.parseInt(reported) - Integer.parseInt(repaired)));
                list.add(recent);
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 发起报修
     *
     * @param userTel
     * @param distinctId
     * @param buildingId
     * @param roomId
     * @param equipmentId
     * @param listDescription
     * @return
     */
    public Report report(String userTel, String distinctId, String buildingId, String roomId, String equipmentId, String listDescription) {
        HttpResponse response;
        HttpClient client = HttpClients.createDefault();

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//        entityBuilder.addBinaryBody("img",file, ContentType.MULTIPART_FORM_DATA,fileName);
        entityBuilder.addTextBody("userTel", userTel);
        entityBuilder.addTextBody("distinctId", distinctId);
        entityBuilder.addTextBody("buildingId", buildingId);
        entityBuilder.addTextBody("roomId", roomId);
        entityBuilder.addTextBody("equipmentId", equipmentId);
        entityBuilder.addTextBody("listDescription", listDescription);

        HttpEntity entity = entityBuilder.build();
        HttpPost post = new HttpPost(reportUrl);
        post.setEntity(entity);

        try {
            response = client.execute(post);
            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()).replace("&nbsp;", ""));
            if (doc.text().contains("我们已经受理您的报修")) {
                Report report = new Report();
                Elements elements = doc.select(".content .card .card-content-inner .list-block ul li");
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).text().contains("报修单号")) {
                        report.setId(elements.get(i).select(".item-after").text());
                    } else if (elements.get(i).text().contains("报修时间")) {
                        report.setTime(elements.get(i).select(".item-after").text().split("\\.")[0]);
                    } else if (elements.get(i).text().contains("您的手机")) {
                        report.setPhone(elements.get(i).select(".item-after").text());
                    } else if (elements.get(i).text().contains("您所在校区的待办报修单数")) {
                        report.setPending(elements.get(i).select(".item-after").text().split("条")[0]);
                    } else if (elements.get(i).text().contains("您所在校区的在办报修单数")) {
                        report.setRepairing(elements.get(i).select(".item-after").text().split("条")[0]);
                    }
                }
                return report;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 评价
     */
    public void evaluate(String listNumber, String phone, String listScore, String listWord) {
        String url = evaluateUrl;

        HttpClient client = HttpClients.createDefault();
        ArrayList<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("listNumber", listNumber));
        postData.add(new BasicNameValuePair("userTel", phone));
        postData.add(new BasicNameValuePair("listScore", listScore));
        postData.add(new BasicNameValuePair("listWord", listWord));

        HttpPost post = new HttpPost(url);
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        post.setHeader("Accept-Encoding", "gzip, deflate, br");
        post.setHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        post.setHeader("Connection", "keep-alive");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        try {
            post.setEntity(new UrlEncodedFormEntity(postData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            client.execute(post);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
