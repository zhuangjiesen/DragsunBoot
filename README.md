# DragsunBoot
spring boot的整合和工具编写

一些分布式的小工具以及一些自己抽象起来的模块

### consumer模块
```
充当consumer调用者的角色，也是个springboot项目
但是当只需要调用service时，可以使用单元测试test包
```

### provider模块
```
充当生产者角色，是个springboot项目

```

### public 
```
公共部分的代码，有些工具类不想拷来拷去，直接搞成公共模块
```

### 打包运行须知
```
需要先mvn package 打包public 模块依赖，不然其他2个模块会报错

```