package com.situ.mall.job;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.situ.mall.api.admin.AdminClient;
import com.situ.mall.api.product.ProductClient;
import com.situ.mall.common.utils.AliOSSUtil;
import com.situ.mall.common.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DeleteImageJob {

    @Autowired
    private AdminClient adminClient;
    @Autowired
    private ProductClient productClient;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void deleteImage() throws InterruptedException {
        System.out.println("DeleteImageJob.deleteImage");
        // 查询所有微服务使用的图片
        Result<Set<String>> adminResult = adminClient.selectAllImage();
        Result<Set<String>> productResult = productClient.selectAllImage();
        Set<String> dbSet = new HashSet<>();
        // 将数据库中查到的图片添加到集合中
        if(adminResult.getCode() == Result.OK){
            dbSet.addAll(adminResult.getData());
        }
        if(productResult.getCode() == Result.OK){
            dbSet.addAll(productResult.getData());
        }
        // 查询阿里云OSS中的所有图片
        Set<String> ossSet = AliOSSUtil.listFile();
        // 移除数据库中存在的图片，剩下的就是要删除的图片
        // 每次删10张图片
        if(!CollectionUtils.isEmpty(ossSet) && !CollectionUtils.isEmpty(dbSet)){
            ossSet.removeAll(dbSet);
            for (String url : ossSet) {
                System.out.println("已删除图片:" + url);
                AliOSSUtil.deleteFile(url);
                // 每次删除一个图片，等待5秒
                Thread.sleep(5000);
            }
        }
    }
}
