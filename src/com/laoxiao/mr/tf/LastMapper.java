package com.laoxiao.mr.tf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

/**
 * 最后计算
 * @author root
 *
 */
public class LastMapper extends Mapper<LongWritable, Text, Text, Text> {
	//存放微博总数
	public static Map<String,Integer> cmap = null;
	//存放df  统计df：词在多少个微博中出现过。
	public static Map<String, Integer> df = null;

	// 在map方法执行之前   只会被调用一次
	protected void setup(Context context) throws IOException,InterruptedException {
		System.out.println("******************");
		if (cmap == null || cmap.size() == 0 || df == null || df.size() == 0) {
            //得到缓存文件
			URI[] ss = context.getCacheFiles();
			if (ss != null) {
				for (int i = 0; i < ss.length; i++) {
					URI uri = ss[i];
					if (uri.getPath().endsWith("part-r-00003")) {//微博总数
						Path path =new Path(uri.getPath()); //得到part-r-00003的路径
//						FileSystem fs =FileSystem.get(context.getConfiguration());
//						fs.open(path);
						BufferedReader br = new BufferedReader(new FileReader(path.getName()));
						String line = br.readLine();
						if (line.startsWith("count")) {
							String[] ls = line.split("\t");
							cmap = new HashMap<String, Integer>();
							cmap.put(ls[0], Integer.parseInt(ls[1].trim()));
						}
						br.close();
					} else if (uri.getPath().endsWith("part-r-00000")) {//词条的DF
						df = new HashMap<String,Integer>();
						Path path =new Path(uri.getPath());
						BufferedReader br = new BufferedReader(new FileReader(path.getName()));
						String line;
						while ((line = br.readLine()) != null) {
							String[] ls = line.split("\t");
							df.put(ls[0], Integer.parseInt(ls[1].trim()));
						}
						br.close();
					}
				}
			}
		}
	}

	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		FileSplit fs = (FileSplit) context.getInputSplit();
//		System.out.println("--------------------");
		if (!fs.getPath().getName().contains("part-r-00003")) {
			
			String[] v = value.toString().trim().split("\t");
			if (v.length >= 2) {
				//tf 为每一个微博中每个单词的词频
				int tf =Integer.parseInt(v[1].trim());  //tf值
				String[] ss = v[0].split("_");
				
				if (ss.length >= 2) {
					String w = ss[0];  //每个微博中分词以后的各个单词 
					String id=ss[1];   //微博编号
					//cmap为微博总数/get(w)这个单词在微博中出现的次数     s为权重
					double s= tf * Math.log(cmap.get("count")/df.get(w));
					NumberFormat nf =NumberFormat.getInstance();
					//最大小数保留位数
					nf.setMaximumFractionDigits(5);
					//格式为:  微博编号      单词:权重 单词权重...
					context.write(new Text(id), new Text(w+":"+nf.format(s)));
				}
			} else {
				System.out.println(value.toString() + "-------------");
			}
		}
	}
}
