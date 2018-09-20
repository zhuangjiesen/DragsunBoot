package com.dragsun.main;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/20
 */
public class ZookeeperMain {

    /** Zookeeper info */
    private static final String ZK_ADDRESS = "127.0.0.1:2181";


    public static void main(String[] args) throws Exception {

        CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_ADDRESS, new RetryNTimes(10, 5000));
        client.start();

        ZookeeperMain.pathList(client , "/");
    }


    /**
     * 遍历zk的path
     * @author zhuangjiesen
     * @date 2018/9/20 下午5:33
     * @param
     * @return
     */
    public static void pathList(CuratorFramework client , String path) {
        try {
            System.out.println(String.format("path : %s " , path));
            List<String> list = client.getChildren().forPath(path);
            if (CollectionUtils.isEmpty(list)) {
                client.delete().forPath(path);
            }
            if (!path.endsWith("/")) {
                path = path.concat("/");
            }
            for(String mpath : list){
                if (mpath.contains("zookeeper")) {
                    continue;
                }
                System.out.println(String.format("foreach - path : %s " , path + mpath));
                pathList(client, path + mpath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
