# AopPlugin
关于aspectj的配置插件

这个插件是可以编译kotlin文件的，以前的gradle配置只能编译java文件。
<br>

使用方式：
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;第一步: 添加classpath 'com.librity.aop:aopRely:1.0'
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;第二步：

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; * 如果想要每个module都可以使用aspectj，那么你需要在根的build.gradle里面依赖我的插件：
 ```java
 allprojects {
    repositories {
        google()
        mavenLocal()
        jcenter()
    }
    apply plugin: 'aopconfig'  //这是我的插件
}

 ```
 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;* 如果只想在单独的module里面使用的话，你就只需要在单独的module的头部写上 apply plugin: 'aopconfig'

至此就配置完成了，你就可以使用aspectj框架了。
