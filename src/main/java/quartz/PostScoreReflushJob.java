package quartz;

import com.community.service.impl.DiscussPostService;
import com.community.service.impl.ElasticSearchService;
import com.community.service.impl.LikeService;
import com.community.util.CommonStatus;
import com.community.util.RedisUtil;
import com.community.vo.DiscussPost;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/07/20:17
 * @Description:
 */
public class PostScoreReflushJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreReflushJob.class);

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    LikeService likeService;

    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    RedisTemplate redisTemplate;

    //马桶纪元
    private static final Date initTime;

    static {
        try {
            initTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1997-08-03 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化马桶纪元失败!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String scoreKey = RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(scoreKey);

        if (operations.size() == 0) {
            logger.info("【任务取消】：没有需要重新计算权重的帖子");
        }

        logger.info("【任务开始】：需要刷新的帖子数:  + operations.size()");
        while (operations.size() > 0) {
            refush((int) operations.pop());
        }
        logger.info("【任务结束】：刷新完成");
    }

    private void refush(int postId) {
        DiscussPost discussPost = discussPostService.selectDisCussPostById(postId);
        //是否精华
        boolean isGreat = discussPost.getStatus() == 1;
        //评论数
        int commentCount = discussPost.getCommentCount();
        //点赞数
        long likeCount = likeService.getLikeCount(CommonStatus.ENTITY_TYPE_POST, postId);
        //时间权重
        double timeScore = (discussPost.getCreateTime().getTime() - initTime.getTime()) / (1000 * 3600 * 24);
        //主观权重
        double w =  (isGreat?75:0) + commentCount*10 + likeCount*2;
        //最终分数
        double score = Math.log10(Math.max(w,1)) + timeScore;

        discussPost.setScore(score);
        //更新数据库
        discussPostService.updateScore(postId, score);
        //更新es
        elasticSearchService.insert(discussPost);
    }
}
