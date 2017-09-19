package com.xzh.recommend;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;


public class Step1 {
    
	    //Recommend.java，主任务启动程序
		//Step1.java，按用户分组，计算所有物品出现的组合列表，得到用户对物品的评分矩阵
		//Step2.java，对物品组合列表进行计数，建立物品的同现矩阵
		//Step3.java，合并同现矩阵和评分矩阵
		//Step4.java，计算推荐结果列表
		//HdfsDAO.java，HDFS操作工具类
	
    public static class Step1_ToItemPreMapper extends MapReduceBase implements Mapper<Object, Text, IntWritable, Text> {
        private final static IntWritable k = new IntWritable();
        private final static Text v = new Text();

        @Override
        public void map(Object key,Text value,OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {
            String[] tokens = Recommend.DELIMITER.split(value.toString()); 
            int userID = Integer.parseInt(tokens[0]); //用户ID
            String itemID = tokens[1];    //物品ID
            String pref = tokens[2];       //喜好评分
            k.set(userID);
            v.set(itemID + ":" + pref);
            //用户id  物品id:喜好评分
            output.collect(k,v);
        }
    }
    
    //combiner
    public static class Step1_ToUserVectorReducer extends MapReduceBase implements Reducer<IntWritable, Text, IntWritable, Text> {
        private final static Text v = new Text();

		@Override
		public void reduce(IntWritable key,Iterator<Text> values,OutputCollector<IntWritable, Text> output, Reporter arg3) throws IOException {
			StringBuilder sb = new StringBuilder();
            while (values.hasNext()) {
                sb.append("," + values.next());
            }
             
            v.set(sb.toString().replaceFirst(",",""));
            // 
            output.collect(key, v);
			
		}
    }

    public static void run(Map<String,String> path) throws IOException {
        JobConf conf = Recommend.config();

        String input = path.get("Step1Input");
        String output = path.get("Step1Output");

        HdfsDAO hdfs = new HdfsDAO(Recommend.HDFS,conf);
        //如果存在目录，则删除
        hdfs.rmr(input);
        hdfs.mkdirs(input);
        //拷贝数据进入HDFS
        hdfs.copyFile(path.get("data"),input);

        conf.setMapOutputKeyClass(IntWritable.class);
        conf.setMapOutputValueClass(Text.class);

        conf.setOutputKeyClass(IntWritable.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(Step1_ToItemPreMapper.class);
        conf.setCombinerClass(Step1_ToUserVectorReducer.class);
        conf.setReducerClass(Step1_ToUserVectorReducer.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf,new Path(input));
        FileOutputFormat.setOutputPath(conf,new Path(output));

        RunningJob job = JobClient.runJob(conf);
        while (!job.isComplete()) {
            job.waitForCompletion();
        }
    }

}