package com.community.service.impl;

import com.community.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/06/11:28
 * @Description:
 */
@Service
public class DataStatisticsService {

    @Autowired
    RedisTemplate<String, Object> template;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


    /**
     * 插入UV数据
     *
     * @param ip
     */
    public void addUVData(String ip) {
        String UVKey = RedisUtil.generateUVKey(sdf.format(new Date()));
        template.opsForHyperLogLog().add(UVKey, ip);
    }

    /**
     * 统计指定日期范围内的UV
     *
     * @param start
     * @param end
     * @return
     */
    public long statisticsUVData(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        //获取所有日期的UVkey
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String UVKey = RedisUtil.generateUVKey(sdf.format(calendar.getTime()));
            keyList.add(UVKey);
            calendar.add(Calendar.DATE, 1);
        }
        //合并，统计数据
        String key = RedisUtil.generateUVKey(sdf.format(start), sdf.format(end));

        template.opsForHyperLogLog().union(key, keyList.toArray(new String[keyList.size()]));

        return template.opsForHyperLogLog().size(key);
    }

    /**
     * 插入DAU数据
     *
     * @param userId
     */
    public void addDAUData(int userId) {
        String key = RedisUtil.generateDAUKey(sdf.format(new Date()));
        template.opsForValue().setBit(key, userId, true);
    }

    /**
     * 统计指定日期范围内的DAU
     *
     * @param start
     * @param end
     */
    public long statisticsDAUData(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        //获取所有日期的DAUkey
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String DAUKey = RedisUtil.generateDAUKey(sdf.format(calendar.getTime()));
            keyList.add(DAUKey);
            calendar.add(Calendar.DATE, 1);
        }
        byte[][] bytes = new byte[keyList.size()][];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = keyList.get(i).getBytes();
        }
        //合并，统计数据
        return (long) template.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String key = RedisUtil.gengerateDAUKey(sdf.format(start), sdf.format(end));

                connection.bitOp(RedisStringCommands.BitOperation.OR, key.getBytes(),
                        bytes);
                return connection.bitCount(key.getBytes());
            }
        });
    }
}
