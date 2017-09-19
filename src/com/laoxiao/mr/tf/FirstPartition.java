package com.laoxiao.mr.tf;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

/**
 * 第一个MR自定义分区
 * @author root
 *
 */
public class FirstPartition extends HashPartitioner<Text, IntWritable>{

	
	public int getPartition(Text key, IntWritable value, int reduceCount) {
		if(key.equals(new Text("count")))
			//前边的第一个分区new Text(w+"_"+id,第二个分区...
			return 3;  //第三个分区为count
		else
			return super.getPartition(key, value, reduceCount-1);
	}

}
