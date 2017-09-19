package com.laoxiao.mr.tf;


import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class FirstJob {

	public static void main(String[] args) {
		Configuration config =new Configuration();
		config.set("fs.defaultFS", "192.168.214.86:8020");
		config.set("mapred.job.tracker", "192.168.214.86:9001");
		config.set("mapreduce.framework.name", "yarn");
		//config.set("fs.defaultFS", "192.168.214.86:9000");
		config.set("yarn.resourcemanager.hostname", "master");
		try {
			FileSystem fs =FileSystem.get(config);
//			JobConf job =new JobConf(config);
			Job job =Job.getInstance(config);
			job.setJarByClass(FirstJob.class);
			job.setJobName("weibo1");
			
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
//			job.setMapperClass();
			job.setNumReduceTasks(4);
			job.setPartitionerClass(FirstPartition.class);
			job.setMapperClass(FirstMapper.class);
			job.setCombinerClass(FirstReduce.class);
			job.setReducerClass(FirstReduce.class);		
			FileInputFormat.addInputPath(job,new Path("/usr/input/tf-idf"));
			
			Path path =new Path("/usr/output/weibo1");
			if(fs.exists(path)){
				fs.delete(path,true);
			}
			FileOutputFormat.setOutputPath(job,path);
			
			boolean f = job.waitForCompletion(true);
			if(f){
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
