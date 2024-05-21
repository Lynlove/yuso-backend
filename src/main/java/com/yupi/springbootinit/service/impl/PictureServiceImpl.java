package com.yupi.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PictureServiceImpl implements PictureService {
    @Override
    public Page<Picture> searchPicture(String searchText, long pageNum, long pageSize) {

        long index = (pageNum-1)*pageSize;//当前页是2，一页8条，那么就要从（2-1）*8条数据开始显示
        // url里的first参数是指定从第几张图片开始显示,使用占位符动态指定，要求url里本身不带%才能用String.format
        // 如果想排序的话，不用自己实现
        // 直接根据bing排序后的url抓即可。无非是下面多几个参数，在前端没传这几个参数的时候用空串应该就行了
        String url = String.format
                ("https://www.bing.com/images/search?q=%s&first=%s",searchText,index);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片数据获取异常");
        }
        //下面应该是不用再判断抓到的是否为null，抓不到直接抛异常了...
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures=new ArrayList<>();
        for (Element element : elements) {
            // 获取图片地址
            // 通过CSS选择器选取所有具有类名为 "iusc" 的元素。返回的是一个元素列表
            // .get(0) 这个方法获取列表中的第一个元素。
            // .attr("m") 这个方法获取选定元素的属性值
            // 这个需要在浏览器控制台 元素栏下对着看才找得到类名，找到之后可以先调试
            // 看看里面有什么值，再从里面取元素的属性值
            String m = element.select(".iusc").get(0).attr("m");
            Map<String,Object> map= JSONUtil.toBean(m,Map.class);//调试可以看到这个m是个JSON，里面的murl字段才是我们要的图片链接
            String murl = (String)map.get("murl");
            // 获取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            //封装存入，其实用有参构造也行
            Picture Cachepic=new Picture();
            Cachepic.setTitle(title);
            Cachepic.setUrl(murl);
            pictures.add(Cachepic);
            if(pictures.size()>=pageSize)break;//查询pageSize数据就够了
        }
        // 总共爬了多少pic，这个获取不到，就不管total参数了
        Page<Picture>picturePage=new Page<>(pageNum,pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }
}
