package com.community.controller;

import com.community.annotation.CheckLogin;
import com.community.service.impl.FollowService;
import com.community.service.impl.LikeService;
import com.community.service.impl.UserService;
import com.community.util.CommonStatus;
import com.community.util.CommonUtil;
import com.community.util.RedisUtil;
import com.community.util.UserThreadLocal;
import com.community.vo.User;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/16/10:18
 * @Description:
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private UserService userService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;


    /**
     * 进入个人设置页面， 同时生成七牛云的上传凭证
     * @return
     */
    @CheckLogin
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        //上传文件名称
        String fileName = CommonUtil.generateUUID();
        //设置响应信息
        StringMap map = new StringMap();
        map.put("returnBody", CommonUtil.getJSONString(0));
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, map);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    /**
     * 更新头像路径
     * @param fileName
     * @return
     */
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommonUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        User user = UserThreadLocal.getUser();
        user.setHeaderUrl(url);
        userService.updateUserHeaderUrl(user);

        return CommonUtil.getJSONString(0);
    }

    /**
     * 上传头像（上传至本地时，现改为上传至七牛云，已废弃）
     *
     * @param model
     * @param multipartFile
     * @return
     */
    @CheckLogin
    //@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadFile(Model model, MultipartFile multipartFile) {
        if (multipartFile == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.indexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        //将上传的文件存到服务器内
        fileName = CommonUtil.generateUUID() + suffix;
        File file = new File(uploadPath + "/" + fileName);
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //更改User的headUrl
        User user = UserThreadLocal.getUser();
        user.setHeaderUrl(domain + contextPath + "/user/header/" + fileName);
        userService.updateUserHeaderUrl(user);
        return "redirect:/index";
    }

    /**
     * 导航栏的头像显示 （上传至本地时，现改为上传至七牛云，已废弃）
     *
     * @param fileName
     * @param response
     */
    //@RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器的头像存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(new File(fileName));
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1) {
                outputStream.write(buffer);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param model
     * @param OldPassWord
     * @param passWord
     * @return
     */
    @CheckLogin
    @RequestMapping(value = "/changePassWord", method = RequestMethod.POST)
    public String changePassWord(Model model, String OldPassWord, String passWord, String checkPassWord) {
        Map<String, Object> map = userService.updatePassWord(OldPassWord, passWord, checkPassWord);
        if (map == null) {
            return "redirect:/index";
        }
        model.addAttribute("errorMsg", map.get("errorMsg"));
        model.addAttribute("checkMsg", map.get("checkMsg"));
        return "/site/setting";
    }

    /**
     * to:个人主页
     *
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String toUserHomePage(@PathVariable int userId, Model model) {
        User user = userService.selectUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        int likeCount = likeService.getLikeUserCount(userId);
        //当前用户关注总数
        long followeeCount = followService.getFolloweeCount(userId, CommonStatus.ENTITY_TYPE_USER);
        //粉丝总数
        long followerCount = followService.getFollowerCount(CommonStatus.ENTITY_TYPE_USER, userId);
        //当前用户是否被关注(他人主页时)
        boolean hasfollow = false;
        if (UserThreadLocal.getUser() != null){
            hasfollow = followService.hasFollow(UserThreadLocal.getUser().getId(), CommonStatus.ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasfollow);
        model.addAttribute("followeeCount", followeeCount);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("user", user);
        model.addAttribute("likeCount", likeCount);

        return "/site/profile";
    }
}
