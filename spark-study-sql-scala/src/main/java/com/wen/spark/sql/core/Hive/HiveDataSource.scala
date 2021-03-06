package com.wen.spark.sql.core

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.hive.HiveContext

object HiveDataSource {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("HiveDataSource")
    val sc = new SparkContext(conf)
    // 创建HiveContext  注意这里接收的是SparkContext   不是 JavaSparkContext
    val sqlContext = new HiveContext(sc)
    //第一个功能，使用HiveContext的Sql()/Hql
    sqlContext.sql("DROP TABLE IF EXISTS student_info")
    sqlContext.sql("CREATE  TABLE IF NOT EXISTS student_info (name STRING ,age INT)")
    System.out.println("============================create table success")
    //将学生的基本信息导入到StudentInfo  表
    sqlContext.sql("LOAD DATA LOCAL INPATH '/data/hive/student_info/student_info.txt' INTO TABLE  student_info")
    sqlContext.sql("DROP TABLE IF EXISTS student_scores")
    sqlContext.sql("CREATE  TABLE IF NOT EXISTS student_scores (name STRING ,score INT)")
    //将学生的基本分数导入到StudentInfo  表
    sqlContext.sql("LOAD DATA LOCAL INPATH '/data/hive/student_info/student_scores.txt' INTO TABLE  student_scores")
    //第二个功能接着将sql  返回的DataFrame  用于查询
    //执行sql  关联两张表查询大于80分的学生
    val goodStudentDS = sqlContext.sql("SELECT ss.name ,s1.age,ss.score from student_info s1 JOIN  student_scores ss ON s1.name=ss.name WHERE   ss.score>=80")
    //第三个功能，可以将 DataFrame  中的数据 理论上来说DataFrame  对应的RDD  数据  是ROW  即可
    //将DataFrame  保存到Hive  表中·
    //  接着将数据保存到good_student_info  中
    sqlContext.sql("DROP TABLE IF EXISTS good_student_info")
    System.out.println("create table success")
    goodStudentDS.write.saveAsTable("good_student_info")
    //  第四个功能 针对  good_student_info  表  直接创建   DataSet
    val goodStudentDSRows = sqlContext.tables("good_student_info")
    val goodStudentRows = goodStudentDSRows.collect
    for (goodStudentRow <- goodStudentRows) {
      System.out.println(goodStudentRow)
    }
  }
}
