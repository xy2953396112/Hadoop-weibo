package com.laoxiao.mr.tf;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
/**
 * c1_001,2
 * c2_001,1
 * count,10000
 * @author root
 *
 */
public class FirstReduce extends Reducer<Text, IntWritable, Text, IntWritable>{
	
	protected void reduce(Text arg0, Iterable<IntWritable> arg1,Context arg2)
			throws IOException, InterruptedException {
		
		int sum =0;
		for( IntWritable i :arg1 ){
			sum= sum+i.get();   //总共单词的数量
		}
		if(arg0.equals(new Text("count"))){
			System.out.println(arg0.toString() +"___________"+sum); //微博个数
		}
		//微博个数:总共的单词数量
		arg2.write(arg0, new IntWritable(sum));
	}

}
