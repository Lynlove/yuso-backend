package com.yupi.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class SearchVO implements Serializable {

    private List<Picture> pictureList;
    private List<UserVO> userList;
    private List<PostVO> postList;
    private List<?> dataList;//只请求其中一个数据源时可以用dataList
    private static final long serialVersionUID = 1L;
}
