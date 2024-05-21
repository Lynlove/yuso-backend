package com.yupi.springbootinit;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class CrawlerTest {
    @Resource
    private PostService postService;

    @Test
    void testFetchPicture() throws IOException {
        int current = 1;
        String url = "https://www.bing.com/images/search?q=小黑子&first=" + current;
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址 (murl)
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
//            System.out.println(murl);
            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
//            System.out.println(title);
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
        }
        System.out.println(pictures);
    }

    @Test
    void testFetchPassage() {
        // 1. 获取数据
        // 下面这种方式需要在浏览器请求的预览中能直接看到，才可以抓取，加密的抓不了
        // 预览里看JSON格式
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result;
        try( HttpResponse response= HttpRequest.post(url)
                .body(json)//设置请求体
                .execute()//通过 .execute() 方法发送请求。
        ){
            result = response.body();//然后，使用 .body() 方法获取了响应体
        }

        // 2. json 转对象
        // 只需要其中一些字段，不用专门写个类来接收，直接用map接收然后取其中几个字段即可
        // map的key值以及value类型可以在调试窗口看到
        // 判空又判null，如果没抓取到结果就直接返回
        if(StrUtil.isBlank(result))return;
        Map<String,Object> map = JSONUtil.toBean(result,Map.class);
        //null是能强转的，编译不会报错，但是要使用null就会报空指针
        JSONObject data =  Optional.ofNullable((JSONObject)map.get("data")).orElse(new JSONObject());
        JSONArray records =  Optional.ofNullable((JSONArray)data.get("records")).orElse(new JSONArray());

        List<Post>postList=new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRecord = JSONUtil.parseObj(record);
            Post postCache =new Post();
            //使用Optional优雅的判空，当map字段为空时，赋默认值空串
            postCache.setTitle(Optional.ofNullable(tempRecord.getStr("title")).orElse(""));
            postCache.setContent(Optional.ofNullable(tempRecord.getStr("content")).orElse(""));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            //tags不存在赋空.Optional.ofNullable(tags.toList(String.class)),orElse会炸！因为tags为null不能调用toList函数！
            List<String> tagList = Optional.ofNullable(tags).orElse(new JSONArray()).toList(String.class);
            postCache.setTags(JSONUtil.toJsonStr(tagList));
            postCache.setUserId(1L);
            //关于这篇帖子是谁写的，不要用抓取的数据了，万一我们数据库没这个用户就尴尬了
            postList.add(postCache);
        }
        //System.out.println(postList);
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }
}
