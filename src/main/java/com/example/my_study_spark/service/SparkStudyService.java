package com.example.my_study_spark.service;

import com.example.my_study_spark.vo.ReportCountVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.stereotype.Service;
import org.apache.spark.*;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: my_study_spark
 * @description: 学习spark类
 * @author: shouhang.liu
 * @create: 2024-05-20 11:25
 **/
@Slf4j
@Service
public class SparkStudyService {

    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setAppName("appName").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        String jdbcUrl = "jdbc:mysql://192.168.123.5:30471/sls-esm-dev?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false";
        String user = "root";
        String password = "1qaz2wsx";

        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT t1_hour FROM report_count where t1_hour is not null");
            List<Double> reportList = new ArrayList<>();
            while (rs.next()) {
                reportList.add(rs.getDouble("t1_hour"));
            }

            long l = System.currentTimeMillis();
            JavaRDD<Double> data = sc.parallelize(reportList, 1);
//            long count = data.count();
            Double sum = data.reduce((a, b) -> a + b);
            System.out.println("Sum of rows in report_count table: " + sum
            +"-->耗时:"+(System.currentTimeMillis()-l));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        sc.stop();
    }


    private static class ResultSetIterator implements java.util.Iterator<ReportCountVo>, Serializable {
        private ResultSet rs;

        public ResultSetIterator(ResultSet rs) {
            this.rs = rs;
        }

        @Override
        public boolean hasNext() {
            try {
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public ReportCountVo next() {
            try {
                return (ReportCountVo)rs.getObject(1); // Assuming the first column is of type String
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


}
