package com.community.event;

import com.alibaba.fastjson.JSONObject;
import com.community.vo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/01/27/19:44
 * @Description:
 */
@Component
public class EventProducer {

    @Autowired
    KafkaTemplate template;

    /**
     * 事件发生时，将事件发布到指定的主题
     * @param event
     */
    public void dealEvent(Event event){
        template.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
