package com.community.actuator;

import com.community.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: majhp
 * @Date: 2020/02/11/17:42
 * @Description:
 */
@Component
@Endpoint(id = "dbTest")
public class DataBaseEndPoint {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseEndPoint.class);

    @Autowired
    DataSource dataSource;

    @ReadOperation
    public String dbConnectionTest() {
        try (Connection connection = dataSource.getConnection()) {
            return CommonUtil.getJSONString(0, "测试：数据库连接成功！");
        } catch (SQLException e) {
            logger.error("测试：数据库连接失败" + e.getMessage());
            return CommonUtil.getJSONString(1, "测试：数据库连接成功！");
        }
    }
}
