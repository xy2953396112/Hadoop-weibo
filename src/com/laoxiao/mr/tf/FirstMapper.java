package com.laoxiao.mr.tf;

import java.io.IOException;
import java.io.StringReader;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * 第一个MR，计算TF和计算N(微博总数)
 * @author root
 *
 */
public class FirstMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

	protected void map(LongWritable key, Text value,Context context) throws IOException, InterruptedException {
		String[]  v =value.toString().trim().split("\t");
		if(v.length>=2){
		String id=v[0].trim(); //去除两边的空格
		String content =v[1].trim();
		
		StringReader sr =new StringReader(content);
		//分词器   把内容分词
		IKSegmenter ikSegmenter =new IKSegmenter(sr, true);
		Lexeme word=null;
		while( (word=ikSegmenter.next()) != null ){
			String w= word.getLexemeText();  //内容中的每个词汇
			context.write(new Text(w+"_"+id), new IntWritable(1));   //只针对一个微博文本
		}
		context.write(new Text("count"), new IntWritable(1)); 
		}else{
			System.out.println(value.toString()+"-------------");
		}
	}
	
	
	
}
