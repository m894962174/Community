package com.community.controller;

import com.community.service.impl.ElasticSearchService;
import com.community.service.impl.LikeService;
import com.community.service.impl.UserService;
import com.community.util.CommonStatus;
import com.community.vo.DiscussPost;
import com.community.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/01/19:23
 * @Description:
 */
@Controller
public class SearchController {

    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;



    /**
     * 搜索结果
     * @param keyword
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {

        org.springframework.data.domain.Page<DiscussPost> p =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        List<Map<String, Object>> postsVo = new ArrayList<>();
        if(p != null) {
            for(DiscussPost post : p) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.selectUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.getLikeCount(CommonStatus.ENTITY_TYPE_POST, post.getId()));
                postsVo.add(map);
            }
        }
        model.addAttribute("discussPosts", postsVo);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(p == null ? 0 : (int) p.getTotalElements());

        return "/site/search";
    }
}
